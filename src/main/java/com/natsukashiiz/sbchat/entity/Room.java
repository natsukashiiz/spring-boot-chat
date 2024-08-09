package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private RoomType type;

    @ManyToOne
    private User owner;

    private String name;
    private String image;
}