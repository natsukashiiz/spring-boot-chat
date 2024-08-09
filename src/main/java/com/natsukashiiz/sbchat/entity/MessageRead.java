package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "message_reads")
public class MessageRead extends BaseEntity {

    @ManyToOne
    private Message message;

    @ManyToOne
    private User user;
}