package com.techversant.common_lib.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Published by customer-service when a customer is deleted.
 * eventId, eventName and timeStamp come from {@link EventBase}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CustomerDeletedEvent extends EventBase {
    private UUID customerId;
    private UUID userId;
    private Long customerNo;
    private String reason;
}
