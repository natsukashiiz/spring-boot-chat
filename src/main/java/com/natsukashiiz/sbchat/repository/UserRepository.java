package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrMobile(String username, String mobile);
    boolean existsByUsername(String username);
    boolean existsByMobile(String mobile);
}