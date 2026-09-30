package com.techversant.common_lib.events;

import java.time.Instant;
import java.util.UUID;

public class EventBase {
    private String eventId;
    private String eventName;
    private String timeStamp;

    public EventBase() {
        this.eventId = UUID.randomUUID().toString();
        this.timeStamp = Instant.now().toString();
    }

    public EventBase(String eventName) {
        this();
        this.eventName = eventName;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}