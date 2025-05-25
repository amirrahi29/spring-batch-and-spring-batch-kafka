package com.bqs.main.controller;

import com.bqs.main.service.s3.S3BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/s3")
public class S3BatchController {

    @Autowired
    private S3BatchService s3BatchService;

    @GetMapping("/process/{fileKey}")
    public String triggerBatchForFile(@PathVariable String fileKey) {
        boolean result = s3BatchService.triggerBatchForFile(fileKey);
        return result
                ? "Job started for file: " + fileKey
                : "Failed to start job for: " + fileKey;
    }

    @GetMapping("/process-all")
    public ResponseEntity<String> processAllFilesManually() {
        s3BatchService.triggerAllFiles();
        return ResponseEntity.ok("All files submitted for processing.");
    }
}
