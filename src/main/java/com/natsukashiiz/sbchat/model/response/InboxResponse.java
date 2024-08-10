package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link com.natsukashiiz.sbchat.entity.Inbox}
 */
@Getter
@Setter
public class InboxResponse implements Serializable {
    private Long id;
    private RoomResponse room;
    private MessageResponse lastMessage;
    private Integer unreadCount;
}