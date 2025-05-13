package com.bqs.main.batch;

import com.bqs.main.model.Patient;
import com.bqs.main.model.PatientError;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.repositories.PatientRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@StepScope
public class PatientItemProcessor implements ItemProcessor<String, PatientWrapper> {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern ZIP_REGEX = Pattern.compile("^\\d{5}(-\\d{4})?$");

    private final Set<String> seenKeys = new HashSet<>();

    @Autowired
    private PatientRepository patientRepository;

    private final CSVParser parser = new CSVParserBuilder()
            .withSeparator(',')
            .withIgnoreQuotations(false)
            .build();

    private PatientError toError(String rawLine, String message) {
        PatientError error = new PatientError();
        error.setErrorMessage(message);
        error.setRawLine(rawLine);
        return error;
    }

    @Override
    public PatientWrapper process(String line) {
        if (line == null || line.trim().isEmpty() ||
                line.contains("Header") || line.contains("Footer end")) {
            return null;
        }

        String[] parts = line.split(",");
        if (parts.length < 9) {
            return new PatientWrapper(null, false, line, "Invalid record format");
        }

        String firstName       = parts[0].trim();
        String lastName        = parts[1].trim();
        String addressLine1    = parts[2].trim();
        String addressLine2    = parts[3].trim();
        String city            = parts[4].trim();
        String state           = parts[5].trim();
        String zipCode         = parts[6].trim();
        String email           = parts[7].trim();
        String merchantNumber  = parts[8].trim();

        // Validate fields
        if (!EMAIL_REGEX.matcher(email).matches()) {
            return new PatientWrapper(new Patient(), false, line, "Invalid email: " + email);
        }
        if (!ZIP_REGEX.matcher(zipCode).matches()) {
            return new PatientWrapper(new Patient(), false, line, "Invalid zip: " + zipCode);
        }
        if (!StringUtils.hasText(addressLine1)) {
            return new PatientWrapper(new Patient(), false, line, "Address missing");
        }
        if (!addressLine1.matches("[A-Za-z0-9 .\\-#]+")) {
            return new PatientWrapper(new Patient(), false, line, "Invalid address: " + addressLine1);
        }
        if (StringUtils.hasText(addressLine2) && !addressLine2.matches("[A-Za-z0-9 .\\-#]+")) {
            return new PatientWrapper(new Patient(), false, line, "Invalid address: " + addressLine2);
        }

        String dedupKey = email.toLowerCase() + "|" + merchantNumber;

        // Step 1: Check DB duplicate
        if (patientRepository.existsByEmailAndMerchantNumber(email, merchantNumber)) {
            return new PatientWrapper(new Patient(), false, line,"Duplicate in database: " + dedupKey);
        }

        // Step 2: Check in-memory duplicate
        if (!seenKeys.add(dedupKey)) {
            return new PatientWrapper(new Patient(), false, line, "Duplicate in file: " + dedupKey);
        }

        // Passed all checks
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setAddressLine1(addressLine1);
        patient.setAddressLine2(addressLine2);
        patient.setCity(city);
        patient.setState(state);
        patient.setZipCode(zipCode);
        patient.setEmail(email);
        patient.setMerchantNumber(merchantNumber);

        return new PatientWrapper(patient, true);
    }
}
