package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
}