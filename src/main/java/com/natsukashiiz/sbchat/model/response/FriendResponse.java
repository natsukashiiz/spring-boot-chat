package com.natsukashiiz.sbchat.model.response;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.entity.Friend;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link Friend}
 */
@Getter
@Setter
public class FriendResponse implements Serializable {
    private UserResponse profile;
    private FriendStatus status;
}