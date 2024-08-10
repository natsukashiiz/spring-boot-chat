package com.natsukashiiz.sbchat.model.response;

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
    private MessageType type;
    private String content;
    private UserResponse sender;
    private RoomResponse room;
    private LocalDateTime createdAt;
}