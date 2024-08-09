package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SignupRequest {

    private String username;
    private String mobile;

    @ToString.Exclude
    private String password;
}
