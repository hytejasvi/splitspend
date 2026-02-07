# Domain Design & Entity Relationships

## 📋 Table of Contents
- [Overview](#overview)
- [Domain Boundaries](#domain-boundaries)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Relationship Semantics](#relationship-semantics)
- [Cascade Behavior](#cascade-behavior)
- [Design Decisions](#design-decisions)
- [Common Patterns](#common-patterns)

---

## Overview

SplitSpend follows **Domain-Driven Design (DDD)** principles with a **modular monolith** architecture. The system is organized into bounded contexts that map to business domains, making future microservices extraction straightforward.

**Core Philosophy:**
- **Correctness over performance** (optimize later with read models)
- **Explicit relationships** (no hidden joins or magic)
- **Fail-fast validation** (enforce constraints at database level)
- **Immutable audit trail** (ledger-based for expenses)

---

## Domain Boundaries
```
com.tejas.splitspend
├── user/          # Identity & authentication
├── group/         # Group management & membership
├── expense/       # Expense creation & splits (Phase 2)
└── settlement/    # Balance calculation & optimization (Phase 3)
```

### Domain Isolation Rules
1. **User domain** has no dependencies (leaf domain)
2. **Group domain** depends on User (via FK, not entity reference in some cases)
3. **Expense domain** depends on Group (expenses belong to groups)
4. **Settlement domain** depends on Expense (settlements derive from expenses)

---

## Entity Relationship Diagram
```
┌─────────────┐
│    User     │
│─────────────│
│ userId (PK) │
│ name        │
│ email       │
│ phoneNumber │
│ password    │
│ createdAt   │
│ updatedAt   │
└──────┬──────┘
       │
       │ 1
       │
       │ N
       │
┌──────▼──────────────┐         N         ┌─────────────┐
│   GroupMember       │◄──────────────────┤    Group    │
│─────────────────────│                   │─────────────│
│ groupMemberId (PK)  │                   │ groupId (PK)│
│ userId (FK)         │                   │ groupName   │
│ groupId (FK)        │         1         │ createdById │
│ role                │                   │ createdAt   │
│ createdAt           │                   │ updatedAt   │
│ updatedAt           │                   │             │
└─────────────────────┘                   └─────────────┘

     JOIN TABLE                           OWNS RELATIONSHIP
  (Explicit Entity)                    (cascade, orphanRemoval)
```

**Key:**
- `──►` One-to-Many (ownership)
- `◄──` Many-to-One (reference)
- `(PK)` Primary Key
- `(FK)` Foreign Key

---

## Relationship Semantics

### 1. User ← GroupMember (Many-to-One, Unidirectional)

**From GroupMember's perspective:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

**Cardinality:** Many GroupMembers → One User

**Navigation:** `member.getUser()` ✅ | `user.getGroupMembers()` ❌

**Why unidirectional?**
- User domain doesn't need to know about groups (loose coupling)
- Prevents circular dependency between domains
- Queries from User side handled via repository: 
```java
  groupMemberRepository.findByUserId(userId)
```

**Cascade:** NONE - Deleting membership doesn't delete user

**Deletion Rule:**
```java
// Service layer prevents this:
if (user.hasAnyGroupMemberships()) {
    throw new CannotDeleteUserException("User must leave all groups first");
}
```

---

### 2. Group → GroupMember (One-to-Many, Bidirectional)

**From Group's perspective:**
```java
@OneToMany(mappedBy = "group", 
           cascade = CascadeType.ALL, 
           orphanRemoval = true)
private List<GroupMember> members = new ArrayList<>();
```

**From GroupMember's perspective:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "group_id", nullable = false)
private Group group;
```

**Cardinality:** One Group → Many GroupMembers

**Navigation:** `group.getMembers()` ✅ | `member.getGroup()` ✅

**Why bidirectional?**
- Groups need to list members: "Show all people in this group"
- Members need to know their group: "Which group does this membership belong to?"
- Enables efficient queries in both directions

**Mapping Strategy:**
- `mappedBy = "group"` on Group side → GroupMember owns the FK
- Group doesn't have `group_id` column, only in-memory collection
- Database FK: `group_members.group_id → groups.group_id`

**Cascade Behavior:**
```java
cascade = CascadeType.ALL  // DELETE, PERSIST, MERGE, REFRESH, DETACH
```

**What gets cascaded:**
```java
// Scenario 1: Save group with members
Group group = new Group("Goa Trip", userId);
GroupMember member = new GroupMember(group, user, ADMIN);
group.getMembers().add(member);
groupRepository.save(group);
// Result: Both Group AND GroupMember are saved (cascade PERSIST)

// Scenario 2: Delete group
groupRepository.delete(group);
// Result: All GroupMembers are deleted first, then Group (cascade REMOVE)
```

**orphanRemoval = true:**
```java
// Remove member from collection
group.getMembers().remove(member);
groupRepository.save(group);
// Result: GroupMember is DELETED from database (not just unlinked)
```

**Deletion Rule:**
```java
// No restrictions - groups can be freely deleted
// All memberships cascade delete automatically
```

---

### 3. Why Not @ManyToMany?

**Typical @ManyToMany approach:**
```java
// ❌ Don't do this!
@Entity
public class User {
    @ManyToMany
    @JoinTable(name = "group_members",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups;
}
```

**Problems:**
- ❌ Can't add extra fields (role, joinedAt)
- ❌ Hidden join table (no entity, no repository)
- ❌ Can't query memberships independently
- ❌ Can't track "who added whom"
- ❌ Cascade behavior unclear

**Our approach: Explicit Join Entity (GroupMember)**
```java
// ✅ Do this instead!
@Entity
public class GroupMember {
    private User user;
    private Group group;
    private MemberRole role;  // ✅ Can add metadata!
    private ZonedDateTime createdAt;  // ✅ Can track timestamps!
}
```

**Benefits:**
- ✅ Full control over relationship
- ✅ Can add business logic
- ✅ Explicit repository: `GroupMemberRepository`
- ✅ Clear cascade semantics
- ✅ Easier to query and test

---

## Cascade Behavior

### Cascade Types Explained

| Cascade Type | What Happens | Example |
|--------------|--------------|---------|
| **PERSIST** | Saving parent saves children | Save group → save new members |
| **MERGE** | Merging parent merges children | Update group → update members |
| **REMOVE** | Deleting parent deletes children | Delete group → delete members |
| **REFRESH** | Refreshing parent refreshes children | Reload group → reload members |
| **DETACH** | Detaching parent detaches children | Rarely used |
| **ALL** | All of the above | Shorthand for everything |

### SplitSpend Cascade Matrix

| Relationship | Cascade | orphanRemoval | Reason |
|--------------|---------|---------------|--------|
| Group → GroupMember | ALL | true | Group owns membership lifecycle |
| GroupMember → User | NONE | N/A | Membership doesn't own user |
| GroupMember → Group | NONE | N/A | Membership doesn't own group |

### Deletion Flow Examples

**Example 1: Delete a Group**
```java
Group group = groupRepository.findById(1L);
groupRepository.delete(group);

// SQL executed (in order):
// 1. DELETE FROM group_members WHERE group_id = 1;
// 2. DELETE FROM groups WHERE group_id = 1;
```

**Example 2: Try to Delete a User (with memberships)**
```java
User user = userRepository.findById(101L);
userRepository.delete(user);

// Result: FK constraint violation!
// ERROR: Cannot delete user - foreign key constraint from group_members
```

**Example 3: Remove Member from Group**
```java
Group group = groupRepository.findById(1L);
group.getMembers().removeIf(m -> m.getUser().getUserId().equals(101L));
groupRepository.save(group);

// SQL executed:
// DELETE FROM group_members WHERE group_member_id = 501;
// (orphanRemoval = true does this automatically)
```

---

## Design Decisions

### 1. Why Store createdById as Long (not User entity)?

**Option A: Entity Reference (❌ Rejected)**
```java
@ManyToOne
@JoinColumn(name = "created_by")
private User createdBy;
```

**Problems:**
- Creates bidirectional dependency: User ← Group
- User domain now coupled to Group domain
- Makes domain extraction harder later

**Option B: Primitive ID (✅ Chosen)**
```java
@Column(name = "created_by")
private Long createdById;
```

**Benefits:**
- ✅ Loose coupling between domains
- ✅ Group doesn't depend on User entity
- ✅ Easy to refactor when moving to microservices
- ✅ Still have referential info (can query if needed)

---

### 2. Why FetchType.LAZY Everywhere?

**Problem:**
```java
// Without LAZY:
List<GroupMember> members = groupMemberRepository.findAll();
// Fires: 1 query for members + N queries for each user + N queries for each group!
// (N+1 problem)
```

**Solution:**
```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;

// Only loads User when explicitly accessed:
members.get(0).getUser().getName();  // Triggers query NOW
```

**When to fetch eagerly:**
```java
// In repository, use JOIN FETCH:
@Query("SELECT gm FROM GroupMember gm " +
       "JOIN FETCH gm.user " +
       "JOIN FETCH gm.group " +
       "WHERE gm.group.groupId = :groupId")
List<GroupMember> findByGroupWithDetails(@Param("groupId") Long groupId);
```

---

### 3. Why No Soft Delete in Phase 1?

**Soft Delete:**
```java
@Column
private boolean deleted = false;

@Column
private ZonedDateTime deletedAt;
```

**Deferred because:**
- Adds complexity (every query needs `WHERE deleted = false`)
- Complicates unique constraints (can't have duplicate emails if soft-deleted)
- Requires query interceptors or custom repositories
- Phase 1 focuses on correctness, not recoverability

**Will add in Phase 4** when we need:
- Audit compliance
- Undo functionality
- Data retention policies

---

### 4. Why @PrePersist Instead of @CreatedDate?

**@CreatedDate approach:**
```java
@EntityListeners(AuditingEntityListener.class)
@CreatedDate
private ZonedDateTime createdAt;
```

**Requires:**
- `@EnableJpaAuditing` in main class
- Spring Data dependency

**@PrePersist approach:**
```java
@PrePersist
protected void onCreate() {
    createdAt = ZonedDateTime.now();
}
```

**Benefits for Phase 1:**
- ✅ Pure JPA (no Spring Data dependency)
- ✅ Explicit and visible in entity code
- ✅ Easier to understand for learning
- ✅ Can upgrade to auditing later for `@CreatedBy`

---

## Common Patterns

### Pattern 1: Creating a Group with Initial Admin
```java
// Service layer
public Group createGroup(String groupName, Long creatorUserId) {
    // 1. Create group
    Group group = new Group(groupName, creatorUserId);
    group = groupRepository.save(group);
    
    // 2. Add creator as admin
    User creator = userRepository.findById(creatorUserId)
        .orElseThrow(() -> new UserNotFoundException(creatorUserId));
    
    GroupMember adminMembership = new GroupMember(group, creator, MemberRole.ADMIN);
    group.getMembers().add(adminMembership);
    
    // 3. Save (cascade persists membership)
    return groupRepository.save(group);
}
```

### Pattern 2: Querying Members of a Group
```java
// Repository
@Query("SELECT gm FROM GroupMember gm " +
       "JOIN FETCH gm.user " +
       "WHERE gm.group.groupId = :groupId")
List<GroupMember> findMembersByGroupId(@Param("groupId") Long groupId);

// Service layer
public List<UserDTO> getGroupMembers(Long groupId) {
    return groupMemberRepository.findMembersByGroupId(groupId).stream()
        .map(gm -> new UserDTO(gm.getUser(), gm.getRole()))
        .collect(Collectors.toList());
}
```

### Pattern 3: Checking if User Can Delete Account
```java
// Service layer
public void deleteUser(Long userId) {
    long membershipCount = groupMemberRepository.countByUserId(userId);
    
    if (membershipCount > 0) {
        throw new CannotDeleteUserException(
            "User has " + membershipCount + " active group memberships. " +
            "Leave all groups before deleting account."
        );
    }
    
    userRepository.deleteById(userId);
}
```

### Pattern 4: Preventing Last Admin from Leaving
```java
// Service layer
public void leaveGroup(Long userId, Long groupId) {
    GroupMember membership = groupMemberRepository
        .findByUserIdAndGroupId(userId, groupId)
        .orElseThrow(() -> new MembershipNotFoundException());
    
    if (membership.getRole() == MemberRole.ADMIN) {
        long adminCount = groupMemberRepository
            .countByGroupIdAndRole(groupId, MemberRole.ADMIN);
        
        if (adminCount == 1) {
            throw new LastAdminException(
                "Cannot leave: you're the last admin. " +
                "Promote someone else or delete the group."
            );
        }
    }
    
    groupMemberRepository.delete(membership);
}
```

---

## Future Considerations

### Phase 2: Expense Domain
```
Expense
  - expenseId
  - groupId (FK → Group)
  - paidById (FK → User)
  - amount, description
  - splitType (EQUAL, CUSTOM, PERCENTAGE)
  
ExpenseSplit (join entity)
  - expenseId (FK)
  - userId (FK)
  - shareAmount
```

### Phase 3: Settlement Domain
```
BalanceEntry (immutable ledger)
  - balanceEntryId
  - expenseId (FK)
  - fromUserId, toUserId
  - amount
  
Settlement (computed)
  - from multiple BalanceEntries
  - optimized transaction graph
```

### Phase 4: Microservices Refactoring
```java
// Change from entity reference:
@ManyToOne
private User user;

// To primitive ID:
@Column(name = "user_id")
private Long userId;

// Query user via API/message broker
User user = userServiceClient.getUserById(userId);
```

---

## References
- [JPA Cascade Types](https://docs.oracle.com/javaee/7/api/javax/persistence/CascadeType.html)
- [orphanRemoval Explained](https://thorben-janssen.com/remove-entity-mappings-hibernate/)
- [FetchType.LAZY Best Practices](https://vladmihalcea.com/n-plus-1-query-problem/)
- Spring Boot Documentation
