package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class MemberGroupResponse extends UserResponse implements Serializable {
    private boolean isOwner;
}
