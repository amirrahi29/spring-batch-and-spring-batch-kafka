package com.bqs.main.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patient_error")
public class PatientError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rawLine;
    private String errorMessage;

    public PatientError(){}

    public PatientError(Long id, String rawLine, String errorMessage) {
        this.id = id;
        this.rawLine = rawLine;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
