package com.bqs.main.config.kafka.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.bqs.main.model.*;
import com.bqs.main.repositories.*;
import com.bqs.main.service.RedisService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PatientKafkaConsumer {

    @Autowired private PatientRepository patientRepository;
    @Autowired private ErrorRepository errorRepository;
    @Autowired private FileProcessingLogRepository fileLogRepo;
    @Autowired private AmazonS3 s3Client;
    @Autowired private RedisService redisService;

    @Value("${s3.aws.bucket}")
    private String bucketName;

    private static final int BATCH_SIZE = 500;
    private final List<Patient> validBuffer = new ArrayList<>();
    private final List<PatientError> errorBuffer = new ArrayList<>();

    private final Map<String, AtomicInteger> fileRecordCountMap = new ConcurrentHashMap<>();
    private final Set<String> deletedFiles = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> fileStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> validCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> invalidCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> fileSizes = new ConcurrentHashMap<>();

    @KafkaListener(
            topics = "${spring.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${spring.kafka.concurrency}"
    )
    public synchronized void listen(PatientWrapper wrapper) {
        try {
            if (wrapper == null) {
                System.err.println("Null wrapper received");
                return;
            }

            String fileKey = wrapper.getFileKey();
            if (redisService.isProcessed(fileKey)) {
                System.out.println("Redis: Already processed: " + fileKey);
                return;
            }

            fileStartTimes.putIfAbsent(fileKey, System.currentTimeMillis());
            fileRecordCountMap.putIfAbsent(fileKey, new AtomicInteger(0));
            fileRecordCountMap.get(fileKey).incrementAndGet();

            if (wrapper.isValid()) {
                Patient patient = wrapper.getPatient();
                patient.setStatus("DY");
                patient.setReferenceId(UUID.randomUUID().toString());
                validBuffer.add(patient);
                validCounts.merge(fileKey, 1, Integer::sum);
            } else {
                PatientError error = new PatientError();
                error.setRawLine(wrapper.getRawLine());
                error.setMessage(wrapper.getErrorMessage());
                error.setStatus("FAIL");
                error.setReason("Validation");
                error.setField(wrapper.getInvalidField());
                error.setValue(wrapper.getInvalidValue());
                error.setCode(400);
                error.setTrackingId(UUID.randomUUID().toString());
                error.setTransientError(false);
                error.setErrorCode("VAL-001");
                error.setErrorDescription("Validation failed for field: " + wrapper.getInvalidField());
                error.setValidationStatus("FAIL");
                error.setDuplicationStatus("DUPLICATE");
                error.setOfferApiStatus("NOT_APPLICABLE");
                error.setProcessingTimestamp(LocalDateTime.now());
                errorBuffer.add(error);
                invalidCounts.merge(fileKey, 1, Integer::sum);
            }

            if (!fileSizes.containsKey(fileKey)) {
                long size = s3Client.getObjectMetadata(bucketName, fileKey).getContentLength();
                fileSizes.put(fileKey, size);
            }

            if (validBuffer.size() >= BATCH_SIZE || errorBuffer.size() >= BATCH_SIZE) {
                flushToDatabase();
            }

        } catch (Exception e) {
            System.err.println("KafkaConsumer Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 30000)
    public synchronized void scheduledFlush() {
        flushToDatabase();
    }

    @PreDestroy
    public void flushRemaining() {
        flushToDatabase();
    }

    private void flushToDatabase() {
        if (!validBuffer.isEmpty()) {
            patientRepository.saveAll(new ArrayList<>(validBuffer));
            System.out.println("Saved " + validBuffer.size() + " patients to DB");
            validBuffer.clear();
        }

        if (!errorBuffer.isEmpty()) {
            errorRepository.saveAll(new ArrayList<>(errorBuffer));
            System.out.println("Saved " + errorBuffer.size() + " errors to DB");
            errorBuffer.clear();
        }

        for (String fileKey : new HashSet<>(fileRecordCountMap.keySet())) {
            AtomicInteger counter = fileRecordCountMap.get(fileKey);
            if (counter != null && counter.decrementAndGet() <= 0 && deletedFiles.add(fileKey)) {
                try {
                    FileProcessingLog log = new FileProcessingLog();
                    log.setFileName(fileKey);
                    log.setFileType("text/plain");
                    log.setFileSizeInBytes(fileSizes.getOrDefault(fileKey, 0L));
                    log.setTotalRecords(validCounts.getOrDefault(fileKey, 0) + invalidCounts.getOrDefault(fileKey, 0));
                    log.setValidRecords(validCounts.getOrDefault(fileKey, 0));
                    log.setInvalidRecords(invalidCounts.getOrDefault(fileKey, 0));
                    log.setProcessedAt(LocalDateTime.now());
                    log.setDurationInMillis(System.currentTimeMillis() - fileStartTimes.getOrDefault(fileKey, 0L));
                    fileLogRepo.save(log);

                    s3Client.deleteObject(bucketName, fileKey);
                    redisService.markAsProcessed(fileKey);
                    System.out.println("Deleted S3 file and marked in Redis: " + fileKey);
                } catch (Exception e) {
                    System.err.println("Failed to delete S3 file or log audit: " + fileKey);
                    e.printStackTrace();
                }
                fileRecordCountMap.remove(fileKey);
            }
        }
    }
}
