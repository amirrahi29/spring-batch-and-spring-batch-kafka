package com.bqs.main.batch;

import com.bqs.main.model.Patient;
import com.bqs.main.model.PatientError;
import com.bqs.main.repositories.PatientRepository;
import com.bqs.main.repositories.ErrorRepository;
import com.bqs.main.utility.BaseLogger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatientErrorWriter extends BaseLogger implements ItemWriter<Object> {

    private final PatientRepository patientRepository;
    private final ErrorRepository errorRepository;

    public PatientErrorWriter(PatientRepository patientRepository, ErrorRepository errorRepository) {
        this.patientRepository = patientRepository;
        this.errorRepository = errorRepository;
    }

    @Override
    public void write(Chunk<? extends Object> items) {
        List<Patient> patients = new ArrayList<>();
        List<PatientError> errors = new ArrayList<>();

        for (Object item : items) {
            if (item instanceof Patient patient) {
                patients.add(patient);
            } else if (item instanceof PatientError error) {
                errors.add(error);
            }
        }

        if (!patients.isEmpty()) {
            patientRepository.saveAll(patients);
            log.info("Saved {} valid patients", patients.size());
        }
        if (!errors.isEmpty()) {
            errorRepository.saveAll(errors);
            log.warn("Saved {} invalid records to error table", errors.size());
        }
    }

}