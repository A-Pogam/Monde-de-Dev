package com.openclassrooms.p6.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a user in the system.
 */
@Entity
@Data
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;


    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;


    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;


    @Column(name = "createdat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updatedat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "passwordchangedat")
    private LocalDateTime passwordChangedAt;
}