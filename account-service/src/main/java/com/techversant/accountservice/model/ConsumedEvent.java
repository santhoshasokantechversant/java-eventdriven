/**
 * @file ConsumedEvent.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Entity class for ConsumedEvent
 */

package com.techversant.accountservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumed_event")
@Getter
@Setter
public class ConsumedEvent {

    @Id
    @Column(name = "event_id", length = 50)
    private String eventId;

    @Column(name = "event_name", length = 100)
    private String eventName;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @Column(name = "service_name", length = 50)
    private String serviceName = "account-service";

    public ConsumedEvent() {
    }

    public ConsumedEvent(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.consumedAt = LocalDateTime.now();
    }
}
