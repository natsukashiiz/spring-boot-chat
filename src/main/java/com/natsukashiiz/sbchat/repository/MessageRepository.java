package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}