package com.maybank.maybank_assessment.batch;

import com.maybank.maybank_assessment.batch.listener.TransactionSkipListener;
import com.maybank.maybank_assessment.model.entity.Transaction;
import com.maybank.maybank_assessment.repository.TransactionRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    // JPA factory for managing database entities and transactions.
    private final EntityManagerFactory emf;

    @Autowired
    private final TransactionRepository transactionRepository;

    // Reads records line-by-line from a flat file (e.g., CSV, TXT).
    @Bean
    public FlatFileItemReader<Transaction> transactionItemReader() {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionItemReader")
                .resource(new FileSystemResource("src/main/resources/dataSource.txt"))
                .linesToSkip(1) // skip header line if present
                .lineMapper(transactionLineMapper())
                .build();
    }

    // Maps each line of the file to a Transaction object.
    private LineMapper<Transaction> transactionLineMapper() {
        // DefaultLineMapper provides a convenient way to map lines to objects
        DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper<>();
        // Splits a line into fields using a delimiter (e.g., |).
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames("ACCOUNT_NUMBER","TRX_AMOUNT","DESCRIPTION","TRX_DATE","TRX_TIME","CUSTOMER_ID");
        tokenizer.setStrict(true); // expect all fields present
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new TransactionFieldSetMapper());
        return lineMapper;
    }

    // FieldSetMapper converts tokenized fields into a Transaction object.
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

    // Processes and validates each Transaction (deduplication, validation).
    @Bean
    public ItemProcessor<Transaction, Transaction> transactionProcessor() {
        // Use a Set to track unique keys within this batch run
        Set<String> seen = Collections.synchronizedSet(new HashSet<>());
        return item -> {
            String uniqueKey = item.getAccountNumber() + "|" +
                    item.getTrxnAmount() + "|" +
                    item.getDescription() + "|" +
                    item.getTrxnTimestamp() + "|" +
                    item.getCustomerId();
            // In-memory deduplication for this batch run
            if (!seen.add(uniqueKey)) {
                return null; // skip duplicate in file
            }
            // DB existence check
            boolean exists = transactionRepository.existsByAccountNumberAndTrxnAmountAndDescriptionAndTrxnTimestampAndCustomerId(
                    item.getAccountNumber(), item.getTrxnAmount(), item.getDescription(), item.getTrxnTimestamp(), item.getCustomerId()
            );
            if (exists) {
                return null; // skip if already in DB
            }
            item.setProcessed(true);
            if (item.getTrxnAmount().signum() < 0) {
                throw new IllegalArgumentException("Transaction amount must be positive: " + item.getTrxnAmount());
            }
            return item;
        };
    }

    // Writes valid Transaction objects to the database using JPA.
    // This uses the EntityManagerFactory to persist entities.
    @Bean
    public JpaItemWriter<Transaction> transactionItemWriter(){
        JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        // Optionally set a custom transaction manager if needed
        // writer.setTransactionManager(transactionManager);
        return writer;
    }

    // JobRepository is used to manage job execution metadata.
    @Bean
    public Job importTransactionJob(JobRepository jobRepository, Step importStep){
        return new JobBuilder("importTransactionsJob", jobRepository)
                .flow(importStep).end().build();
    }

    // TaskExecutor allows parallel processing of chunks in the step and enables multi-threaded step execution.
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-thread-");
        executor.setConcurrencyLimit(4); // Set max concurrent threads
        return executor;
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager txnManager,
                           FlatFileItemReader<Transaction> reader,
                           ItemProcessor<Transaction, Transaction> processor,
                           JpaItemWriter<Transaction> writer,
                           TransactionSkipListener skipListener,
                           TaskExecutor taskExecutor) {
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
                .taskExecutor(taskExecutor) // Enable multi-threading
                .build();
    }
}
