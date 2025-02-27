package com.inkomoko.smarttask.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "password_reset_requests")
@Table(name="password_reset_requests", schema = "st_user")
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User requester;

    private String email;

    @Column(name = "otp_attempt")
    private int otpAttempt;

    private LocalDateTime otpExpiry;

    private String reference;

    private String otp;

    @Column(columnDefinition = "text")
    private String information;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private int status;
}
