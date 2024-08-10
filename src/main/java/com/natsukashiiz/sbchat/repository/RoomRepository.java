package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByMembersUserIdAndType(Long userId, RoomType type);

    Optional<Room> findByIdAndMembersUserId(Long roomId, Long userId);

    @Modifying
    @Query("UPDATE Room r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = ?1")
    void softDeleteById(Long roomId);
}