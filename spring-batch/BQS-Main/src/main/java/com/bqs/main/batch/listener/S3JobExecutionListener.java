package com.bqs.main.batch.listener;

import com.bqs.main.s3.S3InputStreamFetcher;
import com.bqs.main.utility.BaseLogger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3JobExecutionListener extends BaseLogger implements JobExecutionListener {

    @Autowired
    private S3InputStreamFetcher s3Fetcher;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // no-op
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!schedulerEnabled) {
            log.info("Scheduler disabled, skipping file delete.");
            return;
        }

        String fileKey = jobExecution.getJobParameters().getString("fileKey");
        if (jobExecution.getStatus().isUnsuccessful()) {
            log.warn("Job failed for file: " + fileKey + ", skipping deletion.");
            return;
        }

        try {
            s3Fetcher.deleteFile(fileKey);
            log.info("Successfully deleted file from S3: {}", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: " + fileKey, e);
        }
    }
}

