package com.bqs.main.config.kafka;

import com.bqs.main.model.PatientWrapper;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaPatientWriter implements ItemWriter<PatientWrapper> {

    private final KafkaTemplate<String, PatientWrapper> kafkaTemplate;
    private final String topic;

    public KafkaPatientWriter(KafkaTemplate<String, PatientWrapper> kafkaTemplate,
                              @Value("${spring.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void write(Chunk<? extends PatientWrapper> items) throws Exception {
        for (PatientWrapper wrapper : items) {
            kafkaTemplate.send(topic, wrapper);
        }
    }
}
