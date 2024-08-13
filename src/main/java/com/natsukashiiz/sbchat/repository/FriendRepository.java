package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    List<Friend> findAllByFriendIdAndStatus(Long userId, FriendStatus status);

    List<Friend> findAllByUserIdAndStatus(Long userId, FriendStatus status);

    @Modifying
    @Query("update Friend f set f.deletedAt = CURRENT_TIMESTAMP where f.user.id = ?1 and f.friend.id = ?2")
    void softDeleteByUserIdAndFriendId(Long userId, Long friendId);

    @Modifying
    @Query("update Friend f set f.deletedAt = null where f.user.id = ?1 and f.friend.id = ?2")
    void updateDeleteAtNullByUserIdAndFriendId(Long userId, Long friendId);
}