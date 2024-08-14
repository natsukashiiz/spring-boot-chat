package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.MessageAction;
import com.natsukashiiz.sbchat.common.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "messages")
//@SQLRestriction("deleted_at IS NULL")
public class Message extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private MessageAction action;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String content;

    @ManyToOne
    private User sender;

    @ManyToOne
    private Room room;

    @ManyToOne
    private User mention;

    @ManyToOne
    private Message replyTo;
}