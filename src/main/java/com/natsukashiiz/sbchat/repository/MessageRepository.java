package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}