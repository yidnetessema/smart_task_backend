package com.inkomoko.smarttask.email.models;

import com.inkomoko.smarttask.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "emails", schema = "st_notification")
public class EmailModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String reference;

    private String subject;

    private String body;

    private String receiver;

    private String cc;

    @Column(columnDefinition = "text")
    private String information;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private long createdBy;

    private long updatedBy;

    private EmailStatus status;
}
