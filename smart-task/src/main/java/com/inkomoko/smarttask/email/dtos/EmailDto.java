package com.inkomoko.smarttask.email.dtos;

import com.inkomoko.smarttask.email.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmailDto {
    private long id;

    private String reference;

    private String subject;

    private String body;

    private String receiver;

    private String cc;

    private String information;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long createdBy;

    private long updatedBy;

    private EmailStatus status;
}
