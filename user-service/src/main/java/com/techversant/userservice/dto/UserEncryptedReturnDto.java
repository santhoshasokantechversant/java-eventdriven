package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEncryptedReturnDto {
    private String email;
    private String userName;
    private UUID id;
}
