package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    List<Friend> findAllByUserIdOrFriendIdAndStatus(Long userId, Long friendId, FriendStatus status);
}