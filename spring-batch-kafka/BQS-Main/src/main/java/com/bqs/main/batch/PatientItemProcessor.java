package com.bqs.main.batch;

import com.bqs.main.model.Patient;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.repositories.PatientRepository;
import com.bqs.main.validator.PatientValidator;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@StepScope
public class PatientItemProcessor implements ItemProcessor<String, PatientWrapper> {

    private final PatientValidator validator;
    private final PatientRepository patientRepository;
    private final String fileKey;
    private final Set<String> seenKeys = new HashSet<>();
    private final CSVParser parser;

    public PatientItemProcessor(
            @Value("#{jobParameters['fileKey']}") String fileKey,
            PatientValidator validator,
            PatientRepository patientRepository
    ) {
        this.validator = validator;
        this.fileKey = fileKey != null ? fileKey : "UNKNOWN";
        this.patientRepository = patientRepository;
        this.parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(false).build();
    }

    @Override
    public PatientWrapper process(String line) throws Exception {
        if (line == null || line.trim().isEmpty() ||
                line.contains("Header") || line.contains("Footer end")) {
            return null;
        }

        String[] parts = parser.parseLine(line);
        if (parts.length < 13) {
            return PatientWrapper.invalid(line, "Invalid record: expected 13 fields", fileKey, "FAILED", "INVALID_13RECORDS");
        }

        PatientWrapper wrapper = validator.validate(line);
        if (!wrapper.isValid()) {
            wrapper.setFileKey(fileKey);
            return wrapper;
        }

        Patient patient = wrapper.getPatient();

        // Dedup logic
        String fn = patient.getFirstName().toUpperCase();
        String ln = patient.getLastName().toUpperCase();
        String zip = patient.getZipCode();
        String phone = patient.getPhoneNumber();
        String ssn = patient.getSsn();
        String dob = patient.getDob();

        boolean isDuplicate = false;
        String msg = "";

        if (patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndSsn(fn, ln, zip, phone, ssn)) {
            isDuplicate = true;
            msg = "Duplicate [Name + ZIP + Phone + SSN]";
        } else if (patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndDob(fn, ln, zip, phone, dob)) {
            isDuplicate = true;
            msg = "Duplicate [Name + ZIP + Phone + DOB]";
        } else if (patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumber(fn, ln, zip, phone)) {
            isDuplicate = true;
            msg = "Duplicate [Name + ZIP + Phone]";
        }

        patient.setStatus(isDuplicate ? "D" : "DY");

        String inMemKey = fn + "|" + ln + "|" + zip + "|" + phone;
        if (!seenKeys.add(inMemKey)) {
            return PatientWrapper.invalid(line, "Duplicate in file: " + inMemKey, fileKey, "FAILED", "DUPLICATE_FILE");
        }

        return PatientWrapper.valid(patient, line, fileKey);
    }
}

