package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.FriendStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "friends")
public class Friend extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private User friend;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;
}