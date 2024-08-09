package com.natsukashiiz.sbchat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "room_members")
public class RoomMember extends BaseEntity {

    @ManyToOne
    private Room room;

    @ManyToOne
    private User user;

    private Boolean muted;
}