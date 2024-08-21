package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rooms")
//@SQLRestriction("deleted_at IS NULL")
public class Room extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private RoomType type;

    @ManyToOne
    private User owner;

    private String name;
    private String image;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @OrderBy("id DESC, createdAt DESC")
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @OrderBy("id DESC, createdAt DESC")
    @SQLRestriction("deleted_at IS NULL")
    private List<RoomMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Inbox> inboxes = new ArrayList<>();
}