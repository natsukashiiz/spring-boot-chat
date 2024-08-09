package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {
}