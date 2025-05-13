package com.bqs.main.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

@Component
public class S3InputStreamFetcher {

    private final AmazonS3 s3Client;

    @Value("${s3.aws.bucket}")
    private String bucketName;

    public S3InputStreamFetcher(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public InputStreamResource fetch(String fileKey) {
        S3Object s3Object = s3Client.getObject(bucketName, fileKey);
        return new InputStreamResource(s3Object.getObjectContent());
    }

    public void deleteFile(String fileKey) {
        s3Client.deleteObject(bucketName, fileKey);
    }
}

