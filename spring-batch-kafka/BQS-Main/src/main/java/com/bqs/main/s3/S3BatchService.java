package com.bqs.main.s3;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class S3BatchService {

    private final JobLauncher jobLauncher;
    private final Job patientJob;

    public S3BatchService(JobLauncher jobLauncher, Job patientJob) {
        this.jobLauncher = jobLauncher;
        this.patientJob = patientJob;
    }

    public boolean triggerBatchForFile(String fileKey) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("fileKey", fileKey)
                    .addDate("time", new Date())
                    .toJobParameters();
            jobLauncher.run(patientJob, params);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

