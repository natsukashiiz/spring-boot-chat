package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Setter
@Getter
@ToString
public class CreateGroupRequest {

    private String name;
    private String image;
    private Set<Long> memberIds;
}
