package com.techversant.common_lib.events;

/**
 * Saga compensation: tells user-service to delete the user created for a customer
 * whose creation failed. eventId, eventName and timeStamp come from {@link EventBase}.
 */
public class UserRollbackEvent extends EventBase {
    private String email;
    private String phoneNumber;
    private String reason;

    public UserRollbackEvent() {
        super("user.rollback");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
