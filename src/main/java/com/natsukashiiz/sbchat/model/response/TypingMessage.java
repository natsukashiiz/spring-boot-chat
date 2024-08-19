package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TypingMessage {
    private Long roomId;
    private UserResponse user;
}
