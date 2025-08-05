package com.maybank.maybank_assessment.batch;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BatchJobRunnerTests {

    @Test
    void testRunBatchJob_invokesJobLauncher() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job job = mock(Job.class);

        BatchJobRunner runner = new BatchJobRunner(jobLauncher, job);

        var commandLineRunner = runner.runBatchJob();

        // Run the batch job
        commandLineRunner.run();

        // Verify jobLauncher.run was called with correct job and parameters
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(1)).run(eq(job), paramsCaptor.capture());

        // The parameters should contain a "time" key
        assertTrue(paramsCaptor.getValue().getParameters().containsKey("time"));
    }

    @Test
    void testRunBatchJob_handlesException() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job job = mock(Job.class);

        doThrow(new RuntimeException("fail")).when(jobLauncher).run(any(), any());

        BatchJobRunner runner = new BatchJobRunner(jobLauncher, job);

        var commandLineRunner = runner.runBatchJob();

        // Should not throw, just log error
        commandLineRunner.run();
    }
}
