package com.natsukashiiz.sbchat.model.response;

import com.natsukashiiz.sbchat.common.MessageAction;
import com.natsukashiiz.sbchat.common.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.natsukashiiz.sbchat.entity.Message}
 */
@Getter
@Setter
public class MessageResponse implements Serializable {
    private Long id;
    private MessageAction action;
    private MessageType type;
    private String content;
    private UserResponse sender;
    private RoomResponse room;
    private UserResponse mention;
    private MessageResponse replyTo;
    private LocalDateTime createdAt;
}