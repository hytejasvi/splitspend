package com.tejas.splitspend.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/*
 * Represents a user in the SplitSpend application.
 *
 * Core entity in the user domain. A user can participate in multiple groups
 * and share expenses with other users. This entity focuses solely on user identity
 * and authentication - group membership is managed through GroupMember entity.
 *
 * Design Decisions:
 *  Email and phone number are unique to ensure one account per person
 *  Password stored as hashed string (hashing handled in service layer)
 *  Timestamps auto-managed via JPA lifecycle callbacks (@PrePersist/@PreUpdate)
 *  No cascade operations - user is a leaf entity with no ownership of other entities
 */

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String password; // TODO: Implement BCrypt hashing in service layer

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column
    private ZonedDateTime updatedAt;

    /**
     * Creates a new user with required fields.
     * CreatedAt timestamp is auto-populated via @PrePersist.
     */
    public User(String name, String email, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}