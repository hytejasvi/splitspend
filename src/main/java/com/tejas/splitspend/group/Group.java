package com.tejas.splitspend.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Represents a group where users can share expenses.
 *
 *  Core entity in the group domain. A group acts as a container for shared expenses
 *  and maintains membership through GroupMember join entities. Groups are created by users
 *  and can have multiple members with different roles (ADMIN/MEMBER).
 *
 *Relationships:
 *   Group → GroupMember (One-to-Many, bidirectional)
 *   Owns the GroupMember lifecycle - deleting a group cascades to all memberships
 *   Uses orphanRemoval to automatically delete memberships when removed from collection
 *
 * Cascade Strategy:
 *   CascadeType.ALL: When group is saved/updated/deleted, memberships follow
 *   orphanRemoval=true: Removing member from list deletes membership from DB
 *   Members list is eagerly initialized to prevent NullPointerException
 *
 * Design Decisions:
 *   createdById stored as Long (not entity reference) to avoid circular dependency with User domain
 *   Group deletion policy: Only admins can delete, all memberships cascade delete
 *   Business rule (service layer): Group requires at least one admin at all times
 */

@Getter
@Setter
@Entity
@Table(name = "user_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    /*
     * Bidirectional relationship with GroupMember.
     * mappedBy = "group" indicates GroupMember.group field owns the FK.
     * Cascade and orphanRemoval ensure membership lifecycle follows group.
     */
    @OneToMany(mappedBy = "group",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column
    private ZonedDateTime updatedAt;

    /*
     * User ID who created this group (automatically becomes first admin).
     * Stored as primitive to avoid tight coupling with User domain.
     */
    @Column(name = "created_by")
    private Long createdById;

    /*
     * Creates a new group with name and creator.
     * Members list is pre-initialized, creator must be added as GroupMember separately.
     */
    public Group(String groupName, Long createdById) {
        this.groupName = groupName;
        this.createdById = createdById;
    }

    public Group() {
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