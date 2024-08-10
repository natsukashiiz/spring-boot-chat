package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ApiResponse<T> {
    private int status;
    private T data;
    private Long total;
    private Instant timestamp = Instant.now();
}
