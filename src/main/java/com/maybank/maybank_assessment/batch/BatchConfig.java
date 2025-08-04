package com.maybank.maybank_assessment.batch;

import com.maybank.maybank_assessment.batch.listener.TransactionSkipListener;
import com.maybank.maybank_assessment.model.entity.Transaction;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    // For simplicity, we are using a single EntityManagerFactory for the batch job.
    // In a more complex application, you might want to configure multiple data sources or transaction managers
    // for different batch jobs.
    // This configuration assumes that the EntityManagerFactory is already defined in your application context.
    // If you are using Spring Boot, it will automatically configure the EntityManagerFactory based on
    // your application properties and JPA configuration.
    private final EntityManagerFactory emf;

    @Bean
    public FlatFileItemReader<Transaction> transactionItemReader() {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionItemReader")
                .resource(new FileSystemResource("src/main/resources/dataSource.txt"))
                .linesToSkip(1) // skip header line if present
                .lineMapper(transactionLineMapper())
                .build();
    }

    private LineMapper<Transaction> transactionLineMapper() {
        DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames("ACCOUNT_NUMBER","TRX_AMOUNT","DESCRIPTION","TRX_DATE","TRX_TIME","CUSTOMER_ID");
        tokenizer.setStrict(true); // expect all fields present
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new TransactionFieldSetMapper());
        return lineMapper;
    }

    // FieldSetMapper to convert each line's fields into a Transaction object
    public static class TransactionFieldSetMapper implements FieldSetMapper<Transaction>{

        @Override
        public Transaction mapFieldSet(FieldSet fs) throws BindException {
            Transaction txn = new Transaction();
            txn.setAccountNumber(fs.readLong("ACCOUNT_NUMBER"));
            txn.setTrxnAmount(fs.readBigDecimal("TRX_AMOUNT"));
            txn.setDescription(fs.readString("DESCRIPTION"));
            // Parse date and time fields into a single LocalDateTime
            LocalDate date = LocalDate.parse(fs.readString("TRX_DATE"));        // e.g. "2019-09-12"
            LocalTime time = LocalTime.parse(fs.readString("TRX_TIME"));        // e.g. "11:11:11"
            txn.setTrxnTimestamp(LocalDateTime.of(date, time));
            txn.setCustomerId(fs.readLong("CUSTOMER_ID"));
            return txn;
        }
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> transactionProcessor(){
        return item -> {
            //Example validation: ensure amount is positive
            if(item.getTrxnAmount().signum() < 0){
                throw new IllegalArgumentException("Transaction amount must be positive: " + item.getTrxnAmount());
            }
            // Here you can add any processing logic if needed
            // For now, we will just return the item as is
            return item;
        };
    }

    @Bean
    public JpaItemWriter<Transaction> transactionItemWriter(){
        JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        // Optionally set a custom transaction manager if needed
        // writer.setTransactionManager(transactionManager);
        return writer;
    }

    @Bean
    public Job importTransactionJob(JobRepository jobRepository, Step importStep){
        return new JobBuilder("importTransactionsJob", jobRepository)
                .flow(importStep).end().build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager txnManager,
                           FlatFileItemReader<Transaction> reader,
                           ItemProcessor<Transaction, Transaction> processor,
                           JpaItemWriter<Transaction> writer,
                           TransactionSkipListener skipListener) {
        return new StepBuilder("importStep", jobRepository)
                .<Transaction, Transaction>chunk(50, txnManager) // chunk size 50
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE) // allow skipping any number of bad records
                .skip(FlatFileParseException.class)    // skip format errors (e.g. missing fields)
                .skip(IllegalArgumentException.class)  // skip our validation exceptions
                .listener(skipListener)                // attach skip listener for logging
                .build();
    }
}
