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
    private UserResponse owner;
    private String name;
    private String image;
    private List<UserResponse> members = new ArrayList<>();
}