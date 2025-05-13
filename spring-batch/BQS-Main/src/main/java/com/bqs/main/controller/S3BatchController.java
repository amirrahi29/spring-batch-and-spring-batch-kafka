package com.bqs.main.controller;

import com.bqs.main.s3.S3BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/s3")
public class S3BatchController {

    @Autowired
    private S3BatchService s3BatchService;

    @PostMapping("/process/{fileKey}")
    public String triggerBatchForFile(@PathVariable String fileKey) {
        boolean result = s3BatchService.triggerBatchForFile(fileKey);
        return result
                ? "Job started for file: " + fileKey
                : "Failed to start job for: " + fileKey;
    }
}
