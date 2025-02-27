package com.inkomoko.smarttask.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity()
@Table(uniqueConstraints = @UniqueConstraint(name = "user_role_uk", columnNames = "alias"), schema = "st_user")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String alias;

    @Column(columnDefinition = "text")
    private String information;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long createdBy;

    private long updatedBy;

    private int status;
}
