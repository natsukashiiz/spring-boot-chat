package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inboxes")
public class Inbox extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;

    @ManyToOne
    private Message lastMessage;

    private Integer unreadCount;
}