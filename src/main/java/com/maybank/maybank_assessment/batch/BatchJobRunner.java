package com.maybank.maybank_assessment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobRunner {

    private final JobLauncher jobLauncher;
    private final Job importTransactionJob;

    @Bean
    public CommandLineRunner runBatchJob() {
        return args -> {
            try {
                // Create unique job parameters with a timestamp to ensure the job can be run multiple times
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();

                log.info("Starting batch job: importTransactionsJob");
                jobLauncher.run(importTransactionJob, jobParameters);
                log.info("Batch job completed");
            } catch (Exception e) {
                log.error("Error running batch job", e);
            }
        };
    }
}
