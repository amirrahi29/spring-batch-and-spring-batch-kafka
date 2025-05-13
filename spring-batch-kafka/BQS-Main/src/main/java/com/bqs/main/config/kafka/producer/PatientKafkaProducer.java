package com.bqs.main.config.kafka.producer;

import com.bqs.main.model.PatientWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PatientKafkaProducer {

    @Value("${spring.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, PatientWrapper> kafkaTemplate;

    public PatientKafkaProducer(KafkaTemplate<String, PatientWrapper> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("producerTaskExecutor")
    public void send(PatientWrapper wrapper) {
        kafkaTemplate.send(topic, wrapper.getPatient().getEmail(), wrapper);
    }
}
