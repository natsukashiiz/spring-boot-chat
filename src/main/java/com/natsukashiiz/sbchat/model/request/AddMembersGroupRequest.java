package com.natsukashiiz.sbchat.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class AddMembersGroupRequest {

    private List<Long> memberIds;
}
