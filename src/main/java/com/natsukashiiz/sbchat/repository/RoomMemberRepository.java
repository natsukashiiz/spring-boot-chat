package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
}