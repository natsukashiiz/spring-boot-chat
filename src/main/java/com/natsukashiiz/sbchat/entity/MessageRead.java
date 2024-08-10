package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "message_reads")
@SQLRestriction("deleted_at IS NULL")
public class MessageRead extends BaseEntity {

    @ManyToOne
    private Message message;

    @ManyToOne
    private User user;
}