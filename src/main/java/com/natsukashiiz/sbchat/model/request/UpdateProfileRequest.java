package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UpdateProfileRequest {

    private String username;
    private String mobile;
    private String password;
    private String nickname;
    private String avatar;
}
