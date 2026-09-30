package com.techversant.common_lib.events;

public class AccountCreationFailedEvent extends EventBase {
    private Long customerNo;
    private String reason;
    private String errorDetails;
    private String correlationId;

    // Getters and Setters

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public AccountCreationFailedEvent() {
        super("account.creation.failed");
    }

    public Long getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(Long customerNo) {
        this.customerNo = customerNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}