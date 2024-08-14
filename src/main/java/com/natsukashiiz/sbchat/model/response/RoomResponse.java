package com.natsukashiiz.sbchat.model.response;

import com.natsukashiiz.sbchat.common.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link com.natsukashiiz.sbchat.entity.Room}
 */
@Getter
@Setter
public class RoomResponse implements Serializable {
    private Long id;
    private RoomType type;
    private String name;
    private String image;
    private List<MemberGroupResponse> members = new ArrayList<>();
    private UserResponse friend;
    private List<MessageResponse> messages = new ArrayList<>();
    private Long messageCount;
    private Integer unreadCount;
}