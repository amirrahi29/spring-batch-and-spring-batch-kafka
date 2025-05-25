package com.bqs.main.validator;

import com.bqs.main.model.Patient;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PatientValidator {

    @Autowired
    private PatientRepository patientRepository;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9#_.\\-\\s]+$");
    private static final Pattern CITY_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern STATE_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern ZIP_PATTERN = Pattern.compile("^[0-9\\-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private String fileKey;

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public PatientWrapper validate(String rawLine) {
        String[] parts = rawLine.split(",", -1);

        if (parts.length < 13) {
            return PatientWrapper.invalid(rawLine, "Invalid record: expected 13 fields but got " + parts.length, fileKey, "FAILED", "FIELD_MISSING");
        }

        String correlationId  = parts[0].trim();
        String firstName      = parts[1].trim();
        String lastName       = parts[2].trim();
        String addressLine1   = parts[3].trim();
        String addressLine2   = parts[4].trim();
        String city           = parts[5].trim();
        String state          = parts[6].trim();
        String zipCode        = parts[7].trim();
        String email          = parts[8].trim();
        String merchantNumber = parts[9].trim();
        String ssn            = parts[10].trim();
        String dob            = parts[11].trim();
        String phoneNumber    = parts[12].trim();

        if (firstName.isEmpty() || !NAME_PATTERN.matcher(firstName).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid First Name", fileKey, "FAILED", "INVALID_FIRST_NAME");
        }
        if (lastName.isEmpty() || !NAME_PATTERN.matcher(lastName).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Last Name", fileKey, "FAILED", "INVALID_LAST_NAME");
        }
        if (addressLine1.isEmpty() || !ADDRESS_PATTERN.matcher(addressLine1).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Address Line 1", fileKey, "FAILED", "INVALID_ADDRESS1");
        }
        if (addressLine2.isEmpty() || !ADDRESS_PATTERN.matcher(addressLine2).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Address Line 2", fileKey, "FAILED", "INVALID_ADDRESS2");
        }
        if (city.isEmpty() || !CITY_PATTERN.matcher(city).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid City", fileKey, "FAILED", "INVALID_CITY");
        }
        if (state.isEmpty() || !STATE_PATTERN.matcher(state).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid State", fileKey, "FAILED", "INVALID_STATE");
        }
        if (zipCode.isEmpty() || !ZIP_PATTERN.matcher(zipCode).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Zip Code", fileKey, "FAILED", "INVALID_ZIP");
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Email Address", fileKey, "FAILED", "INVALID_EMAIL");
        }
        if (merchantNumber.isEmpty() || !NUMERIC_PATTERN.matcher(merchantNumber).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid MID", fileKey, "FAILED", "INVALID_MID");
        }
        if (ssn.isEmpty() || !NUMERIC_PATTERN.matcher(ssn).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid SSN", fileKey, "FAILED", "INVALID_SSN");
        }
        if (dob.isEmpty()) {
            return PatientWrapper.invalid(rawLine, "DOB is mandatory", fileKey, "FAILED", "MISSING_DOB");
        }
        if (phoneNumber.isEmpty() || !NUMERIC_PATTERN.matcher(phoneNumber).matches()) {
            return PatientWrapper.invalid(rawLine, "Invalid Phone Number", fileKey, "FAILED", "INVALID_PHONE");
        }
        if (correlationId.isEmpty()) {
            return PatientWrapper.invalid(rawLine, "Correlation ID is mandatory", fileKey, "FAILED", "MISSING_CORRELATION_ID");
        }
        // === Duplicate Check ===
        boolean dup1 = patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndSsn(
                firstName, lastName, zipCode, phoneNumber, ssn);
        boolean dup2 = patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndDob(
                firstName, lastName, zipCode, phoneNumber, dob);
        boolean dup3 = patientRepository.existsByFirstNameAndLastNameAndZipCodeAndPhoneNumber(
                firstName, lastName, zipCode, phoneNumber);

        if (dup1 || dup2 || dup3) {
            return PatientWrapper.invalid(rawLine, "Duplicate record found in DB", fileKey, "FAILED", "MISSING dup1 || dup2 || dup3");
        }
        Patient patient = new Patient();
        patient.setCorrelationID(correlationId);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setAddressLine1(addressLine1);
        patient.setAddressLine2(addressLine2);
        patient.setCity(city);
        patient.setState(state);
        patient.setZipCode(zipCode);
        patient.setEmail(email);
        patient.setMerchantNumber(merchantNumber);
        patient.setSsn(ssn);
        patient.setDob(dob);
        patient.setPhoneNumber(phoneNumber);
        return PatientWrapper.valid(patient, rawLine, fileKey);
    }
}
