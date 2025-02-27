package com.inkomoko.smarttask.security.dto;


import com.inkomoko.smarttask.user.Enums.Gender;
import com.inkomoko.smarttask.user.models.UserRole;
import com.inkomoko.smarttask.security.Enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String userName;

    private Gender gender;

    private LocalDate birthDate;

    private String status;

    private UserRole role;

    private String token;


    private String message;

    private ResponseStatus responseStatus;

    private LocalDateTime createdAt;
}
