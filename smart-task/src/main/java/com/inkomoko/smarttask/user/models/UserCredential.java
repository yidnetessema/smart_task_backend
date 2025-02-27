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
@Entity(name = "user_credentials")
@Table(name="user_credentials", schema = "st_user")
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int attempt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    @Column(name = "is_new")
    private int isNew;

    @Column(name = "password_history")
    private String passwordHistory;

    @OneToOne
    private User user;

    @Column(columnDefinition = "text")
    private String information;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long createdBy;

    private long updatedBy;

    private int status;
}