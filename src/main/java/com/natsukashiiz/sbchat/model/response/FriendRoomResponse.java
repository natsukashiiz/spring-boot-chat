package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class FriendRoomResponse extends RoomResponse implements Serializable {

    private UserResponse friend;
}
