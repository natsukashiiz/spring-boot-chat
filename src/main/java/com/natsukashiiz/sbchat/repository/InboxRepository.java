package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InboxRepository extends JpaRepository<Inbox, Long> {
    List<Inbox> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Inbox> findByRoomIdAndUserId(Long roomId, Long userId);
    List<Inbox> findByRoomIdAndUserIdNot(Long roomId, Long userId);
    List<Inbox> findAllByRoomId(Long roomId);
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
}