package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UpdateGroupRequest {

    private String name;
    private String image;
}
