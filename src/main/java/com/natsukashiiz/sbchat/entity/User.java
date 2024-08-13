package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
//@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    private String username;
    private String mobile;
    private String password;
    private String nickname;
    private String avatar;
    private LocalDateTime lastSeenAt;

    @PrePersist
    public void setLastSeenAt() {
        this.lastSeenAt = LocalDateTime.now();
    }
}