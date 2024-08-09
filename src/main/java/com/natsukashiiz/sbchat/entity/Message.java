package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String content;

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;
}