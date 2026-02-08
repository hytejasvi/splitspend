package com.tejas.splitspend.group;

import com.tejas.splitspend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/*
 * Join entity representing the many-to-many relationship between User and Group.
 *
 * This entity captures group membership with additional metadata like role and join date.
 * It's the bridge that allows users to belong to multiple groups and groups to have multiple users.
 *
 *Relationships:
 *   GroupMember → Group (Many-to-One, bidirectional with Group.members)
 *   GroupMember → User (Many-to-One, unidirectional from GroupMember side)
 *   No cascade operations on either side - preserves User and Group independently
 *
 *Lazy Loading Strategy:
 *   FetchType.LAZY on both relationships prevents N+1 query problems
 *   User and Group only loaded when explicitly accessed via getters
 *   Use JOIN FETCH in repositories when related data is needed
 *
 *Deletion Semantics:
 *   Delete Group → GroupMember deleted (via Group's cascade)</li>
 *   Delete User → BLOCKED if any memberships exist (enforced in service layer)</li>
 *   Delete GroupMember → Neither Group nor User affected
 */

@Getter
@Setter
@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupMemberId;

    /*
     * Reference to the group this membership belongs to.
     * FetchType.LAZY to avoid loading group data unless explicitly needed.
     * No cascade - deleting membership does not delete the group.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /*
     * Reference to the user who is a member.
     * FetchType.LAZY to avoid N+1 queries when loading multiple memberships.
     * No cascade - deleting membership does not delete the user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * Member's role in this group (ADMIN or MEMBER).
     * Stored as STRING in DB for human readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column
    private ZonedDateTime updatedAt;

    /*
     * Creates a new group membership.
     * CreatedAt is auto-populated via @PrePersist.
     */
    public GroupMember(Group group, User user, MemberRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    public GroupMember() {
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