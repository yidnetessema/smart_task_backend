package com.inkomoko.smarttask.user.dtos;


import com.inkomoko.smarttask.user.Enums.Gender;
import com.inkomoko.smarttask.user.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;

    private String firstName;

    private String lastName;

    private String email;

    private Gender gender;

    private LocalDate birthDate;

    private String userName;

    private String phoneNumber;

    private Map<String, Object> information;

    private int status;

    private UserRole role;

    private String token;

    private long createdBy;

    private long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
