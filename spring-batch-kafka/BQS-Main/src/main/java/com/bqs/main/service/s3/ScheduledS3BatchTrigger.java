package com.bqs.main.service.s3;

import com.bqs.main.utility.BaseLogger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.Date;
import java.util.List;

@Component
public class ScheduledS3BatchTrigger extends BaseLogger {

    private final AmazonS3 s3Client;
    private final JobLauncher jobLauncher;
    private final Job patientJob;

    @Value("${s3.aws.bucket}")
    private String bucketName;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public ScheduledS3BatchTrigger(AmazonS3 s3Client, JobLauncher jobLauncher, Job patientJob) {
        this.s3Client = s3Client;
        this.jobLauncher = jobLauncher;
        this.patientJob = patientJob;
    }

    @Scheduled(cron = "${app.scheduler.cron}")
    public void runForEachFile() {
        if (!schedulerEnabled) {
            log.info("Scheduler is disabled. Skipping run.");
            return;
        }
        log.info("Scheduler triggered!");
        List<S3ObjectSummary> files = s3Client.listObjects(bucketName).getObjectSummaries();

        for (S3ObjectSummary file : files) {
            String fileKey = file.getKey();
            if (!fileKey.endsWith(".txt")) continue;

            try {
                JobParameters params = new JobParametersBuilder()
                        .addString("fileKey", fileKey)
                        .addDate("time", new Date())
                        .toJobParameters();

                log.info("Triggering batch job for file: {}", fileKey);
                JobExecution execution = jobLauncher.run(patientJob, params);

                if (execution.getStatus().isUnsuccessful()) {
                    log.warn("Batch job failed for file: {}", fileKey);
                    continue;
                }
            } catch (Exception e) {
                log.error("Failed to process file: " + fileKey, e);
            }
        }
    }
}
