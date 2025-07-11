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
 * Entity representing a subscription in the system.
 */
@Entity
@Data
@Table(name = "subscriptions")
public class Subscriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false, insertable = false, updatable = false)
    private Users user;


    @Column(name = "userid")
    private Long userId;


    @ManyToOne
    @JoinColumn(name = "themeid", nullable = false, insertable = false, updatable = false)
    private Themes theme;


    @Column(name = "themeid")
    private Long themeId;


    @Column(name = "issubscribed")
    private Boolean isSubscribed;


    @Column(name = "createdat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


    @Column(name = "updatedat", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
