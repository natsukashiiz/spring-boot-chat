package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    List<RoomMember> findAllByRoomId(Long roomId);

    @Modifying
    @Query("UPDATE RoomMember rm SET rm.deletedAt = CURRENT_TIMESTAMP WHERE rm.room.id = ?1 AND rm.user.id = ?2")
    void softDeleteByRoomIdAndUserId(Long roomId, Long userId);
}