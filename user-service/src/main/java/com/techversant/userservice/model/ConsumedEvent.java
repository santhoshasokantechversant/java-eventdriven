package com.techversant.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumed_event")
public class ConsumedEvent {

    @Id
    @Column(name = "event_id", length = 50)
    private String eventId;

    @Column(name = "event_name", length = 100)
    private String eventName;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @Column(name = "service_name", length = 50)
    private String serviceName = "user-service";

    public ConsumedEvent() {}

    public ConsumedEvent(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.consumedAt = LocalDateTime.now();
    }

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

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
