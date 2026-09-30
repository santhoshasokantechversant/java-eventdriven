package com.techversant.common_lib.events;

/**
 * Saga compensation: tells account-service to delete the account of a customer
 * whose creation failed. eventId, eventName and timeStamp come from {@link EventBase}.
 */
public class AccountRollbackEvent extends EventBase {
    private Long customerNo;
    private String reason;

    public AccountRollbackEvent() {
        super("account.rollback");
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
}
