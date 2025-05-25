package com.bqs.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_error")
public class PatientError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;
    private String field;

    @Column(name = "invalid_value")
    private String value;

    private int code;

    @Column(length = 1000)
    private String message;

    private String trackingId;
    private boolean transientError;
    private String errorCode;

    @Column(length = 1000)
    private String errorDescription;

    private String validationStatus;
    private String duplicationStatus;
    private String offerApiStatus;
    private LocalDateTime processingTimestamp;

    @Column(length = 2000)
    private String rawLine;

    private String status;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public boolean isTransientError() { return transientError; }
    public void setTransientError(boolean transientError) { this.transientError = transientError; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }

    public String getValidationStatus() { return validationStatus; }
    public void setValidationStatus(String validationStatus) { this.validationStatus = validationStatus; }

    public String getDuplicationStatus() { return duplicationStatus; }
    public void setDuplicationStatus(String duplicationStatus) { this.duplicationStatus = duplicationStatus; }

    public String getOfferApiStatus() { return offerApiStatus; }
    public void setOfferApiStatus(String offerApiStatus) { this.offerApiStatus = offerApiStatus; }

    public LocalDateTime getProcessingTimestamp() { return processingTimestamp; }
    public void setProcessingTimestamp(LocalDateTime processingTimestamp) { this.processingTimestamp = processingTimestamp; }

    public String getRawLine() { return rawLine; }
    public void setRawLine(String rawLine) { this.rawLine = rawLine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}