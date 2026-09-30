package com.techversant.common_lib.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUserDto {
    private UUID userId;
    private UUID customerId;
}
