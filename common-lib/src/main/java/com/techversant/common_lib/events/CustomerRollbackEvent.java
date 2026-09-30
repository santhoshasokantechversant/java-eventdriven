package com.techversant.common_lib.events;

import java.util.UUID;

public class CustomerRollbackEvent extends EventBase {
    private Long customerNo;
    private String reason;
    private UUID userId;

    // Getters and Setters


    public Long getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(Long customerNo) {
        this.customerNo = customerNo;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
