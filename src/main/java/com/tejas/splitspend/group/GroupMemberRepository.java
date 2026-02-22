package com.tejas.splitspend.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUser_UserId(Long userId);
}
