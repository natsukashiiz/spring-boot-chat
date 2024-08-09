package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<Inbox, Long> {
}