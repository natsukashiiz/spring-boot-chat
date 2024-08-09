package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class SuccessResponse {
    private int status;
    private Object data;
    private Long total;
    private Instant timestamp = Instant.now();
}
