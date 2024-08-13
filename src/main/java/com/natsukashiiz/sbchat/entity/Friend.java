package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.FriendStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "friends")
//@SQLRestriction("deleted_at IS NULL")
public class Friend extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private User friend;

    @ManyToOne
    private Room room;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;
}