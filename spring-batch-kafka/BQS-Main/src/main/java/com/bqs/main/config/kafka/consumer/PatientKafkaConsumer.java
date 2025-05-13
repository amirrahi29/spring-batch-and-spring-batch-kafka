package com.bqs.main.config.kafka.consumer;

import com.bqs.main.model.PatientError;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.model.Patient;
import com.bqs.main.repositories.ErrorRepository;
import com.bqs.main.repositories.PatientRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import java.util.*;

@Service
public class PatientKafkaConsumer {

    private final PatientRepository patientRepository;
    private final ErrorRepository errorRepository;

    private final List<Patient> validBuffer = new ArrayList<>();
    private final List<com.bqs.main.model.PatientError> errorBuffer = new ArrayList<>();
    private static final int BATCH_SIZE = 500;

    public PatientKafkaConsumer(PatientRepository patientRepository, ErrorRepository errorRepository) {
        this.patientRepository = patientRepository;
        this.errorRepository = errorRepository;
    }

    @KafkaListener(topics = "${spring.kafka.topic}", concurrency = "${spring.kafka.concurrency}", groupId = "${spring.kafka.groupId}")
    public synchronized void listen(PatientWrapper wrapper) {
        if (wrapper.isValid()) {
            validBuffer.add(wrapper.getPatient());
            if (validBuffer.size() >= BATCH_SIZE) {
                patientRepository.saveAll(validBuffer);
                validBuffer.clear();
            }
        } else {
            PatientError pe = new PatientError();
            pe.setRawLine(wrapper.getRawLine());
            pe.setErrorMessage(wrapper.getErrorMessage());
            errorBuffer.add(pe);
            if (errorBuffer.size() >= BATCH_SIZE) {
                errorRepository.saveAll(errorBuffer);
                errorBuffer.clear();
            }
        }
    }

    @PreDestroy
    public void flush() {
        if (!validBuffer.isEmpty()) patientRepository.saveAll(validBuffer);
        if (!errorBuffer.isEmpty()) errorRepository.saveAll(errorBuffer);
    }
}

