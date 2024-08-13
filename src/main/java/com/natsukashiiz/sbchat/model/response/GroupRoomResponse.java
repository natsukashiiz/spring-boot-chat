package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GroupRoomResponse extends RoomResponse implements Serializable {
    private List<MemberGroupResponse> members = new ArrayList<>();
}
