package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "room_members")
@SQLRestriction("deleted_at IS NULL")
public class RoomMember extends BaseEntity {

    @ManyToOne(cascade = CascadeType.ALL)
    private Room room;

    @ManyToOne
    private User user;

    private Boolean muted;
}