package com.bqs.main.model;

public class PatientWrapper {

    private Patient patient;
    private boolean valid;
    private String rawLine;
    private String errorMessage;

    public PatientWrapper() {}

    public PatientWrapper(Patient patient, boolean valid) {
        this.patient = patient;
        this.valid = valid;
    }

    public PatientWrapper(Patient patient, boolean valid, String rawLine, String errorMessage) {
        this.patient = patient;
        this.valid = valid;
        this.rawLine = rawLine;
        this.errorMessage = errorMessage;
    }

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
}
