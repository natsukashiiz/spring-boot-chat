package com.natsukashiiz.sbchat.model.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.natsukashiiz.sbchat.entity.User}
 */
@Getter
@Setter
public class UserResponse implements Serializable {
    private Long id;
    private String username;
    private String mobile;
    private String nickname;
    private String avatar;
    private LocalDateTime lastSeenAt;
}