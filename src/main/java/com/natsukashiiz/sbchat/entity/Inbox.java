package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "inboxes")
//@SQLRestriction("deleted_at IS NULL")
public class Inbox extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;

    @ManyToOne
    private Message lastMessage;

    private Integer unreadCount;

    @PrePersist
    public void prePersist() {
            this.unreadCount = 0;
    }
}