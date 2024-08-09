package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ErrorResponse {
    private int status;
    private String error;
    private LocalDateTime timestamp = LocalDateTime.now();
}
