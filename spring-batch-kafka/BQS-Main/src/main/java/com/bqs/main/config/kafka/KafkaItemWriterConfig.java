package com.bqs.main.config.kafka;

import com.bqs.main.config.kafka.producer.PatientKafkaProducer;
import com.bqs.main.model.PatientWrapper;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaItemWriterConfig {

    private final PatientKafkaProducer producer;

    public KafkaItemWriterConfig(PatientKafkaProducer producer) {
        this.producer = producer;
    }

    @Bean
    public ItemWriter<PatientWrapper> patientKafkaWriter() {
        return items -> {
            for (PatientWrapper wrapper : items) {
                producer.send(wrapper);
            }
        };
    }
}
