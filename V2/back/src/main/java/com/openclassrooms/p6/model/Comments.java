package com.openclassrooms.p6.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity representing a comment in the system.
 */
@Entity
@Data
@Table(name = "comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "userid", nullable = false, insertable = false, updatable = false)
    private Users user;

    @Column(name = "userid")
    private Long userId;


    @ManyToOne
    @JoinColumn(name = "articleid", nullable = false, insertable = false, updatable = false)
    private Articles article;


    @Column(name = "articleid")
    private Long articleId;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "createdat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updatedat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
