package com.bqs.main.batch.listener;

import com.amazonaws.services.s3.AmazonS3;
import com.bqs.main.service.s3.S3InputStreamFetcher;
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

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${s3.aws.bucket}")
    private String bucketName;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String fileKey = jobExecution.getJobParameters().getString("fileKey");
        if (!amazonS3.doesObjectExist(bucketName, fileKey)) {
            throw new RuntimeException("S3 file does not exist: " + fileKey);
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String fileKey = jobExecution.getJobParameters().getString("fileKey");

        if (fileKey == null || fileKey.isBlank()) {
            log.warn("No fileKey provided. Skipping deletion.");
            return;
        }

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.warn("Job failed for file: {}. Skipping S3 deletion.", fileKey);
            return;
        }

    }
}
