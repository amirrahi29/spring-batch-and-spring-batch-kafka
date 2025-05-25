package com.bqs.main.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class S3BatchService {

    private final JobLauncher jobLauncher;
    private final Job patientJob;

    @Autowired
    private AmazonS3 s3Client;
    @Value("${s3.aws.bucket}")
    private String bucketName;

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

    public void triggerAllFiles() {
        List<S3ObjectSummary> files = s3Client.listObjects(bucketName).getObjectSummaries();
        for (S3ObjectSummary file : files) {
            String fileKey = file.getKey();
            triggerBatchForFile(fileKey);
        }
    }

}

