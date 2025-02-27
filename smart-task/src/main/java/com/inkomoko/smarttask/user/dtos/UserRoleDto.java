package com.inkomoko.smarttask.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDto {
    private long id;

    private String name;

    private String alias;

    private String information;

    private LocalDateTime createdAt;

    private long createdBy;

    private int status;

    private long users;

    private int isStaffRole;
}
