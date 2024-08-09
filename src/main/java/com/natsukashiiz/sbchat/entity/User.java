package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String username;
    private String mobile;
    private String password;
    private String nickname;
    private String avatar;
    private LocalDateTime lastSeenAt;
}