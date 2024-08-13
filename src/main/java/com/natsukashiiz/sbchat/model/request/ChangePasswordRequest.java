package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePasswordRequest {

    private String cur;
    private String latest;
}
