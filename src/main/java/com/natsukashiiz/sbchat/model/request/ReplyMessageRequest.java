package com.natsukashiiz.sbchat.model.request;

import com.natsukashiiz.sbchat.common.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ReplyMessageRequest {

    private MessageType type;
    private String content;
    private Long replyToId;
}
