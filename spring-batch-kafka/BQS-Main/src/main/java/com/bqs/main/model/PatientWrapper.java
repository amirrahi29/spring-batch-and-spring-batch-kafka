package com.bqs.main.model;

import java.io.Serializable;

public class PatientWrapper implements Serializable {

    private Patient patient;
    private  boolean valid;
    private String rawLine;
    private String errorMessage;
    private String fileKey;
    private String invalidField;
    private String invalidValue;

    public PatientWrapper() {}

    public PatientWrapper(Patient patient, boolean valid) {
        this.patient = patient;
        this.valid = valid;
    }

    // Factory method for valid record
    public static PatientWrapper valid(Patient patient, String rawLine, String fileKey) {
        PatientWrapper pw = new PatientWrapper();
        pw.setPatient(patient);
        pw.setValid(true);
        pw.setRawLine(rawLine);
        pw.setFileKey(fileKey);
        return pw;
    }

    // Updated Factory Method for invalid record
    public static PatientWrapper invalid(String rawLine, String errorMessage, String fileKey,
                                         String invalidField, String invalidValue) {
        PatientWrapper pw = new PatientWrapper();
        pw.setValid(false);
        pw.setRawLine(rawLine);
        pw.setErrorMessage(errorMessage);
        pw.setFileKey(fileKey);
        pw.setInvalidField(invalidField);
        pw.setInvalidValue(invalidValue);
        return pw;
    }


    //(You can keep the existing valid() method unchanged)
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getInvalidField() {
        return invalidField;
    }

    public void setInvalidField(String invalidField) {
        this.invalidField = invalidField;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }

    @Override
    public String toString() {
        return "PatientWrapper{" +
                "valid=" + valid +
                ", rawLine='" + rawLine + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", fileKey='" + fileKey + '\'' +
                ", invalidField='" + invalidField + '\'' +
                ", invalidValue='" + invalidValue + '\'' +
                ", patient=" + (patient != null ? patient.getFirstName() + " " + patient.getLastName() : "null") +
                '}';
    }
}
