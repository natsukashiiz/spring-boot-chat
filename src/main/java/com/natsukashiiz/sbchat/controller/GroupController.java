package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.AddMembersGroupRequest;
import com.natsukashiiz.sbchat.model.request.CreateGroupRequest;
import com.natsukashiiz.sbchat.model.request.UpdateGroupRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.RoomResponse;
import com.natsukashiiz.sbchat.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ApiResponse<List<RoomResponse>> getGroups() throws BaseException {
        return groupService.getGroups();
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getGroup(@PathVariable Long roomId) throws BaseException {
        return groupService.getGroup(roomId);
    }

    @PostMapping
    public ApiResponse<RoomResponse> createGroup(@RequestBody CreateGroupRequest request) throws BaseException {
        return groupService.createGroup(request);
    }

    @PutMapping("/{roomId}")
    public ApiResponse<RoomResponse> updateGroup(@PathVariable Long roomId, @RequestBody UpdateGroupRequest request) throws BaseException {
        return groupService.updateGroup(roomId, request);
    }

    @PostMapping("/{roomId}/members")
    public ApiResponse<RoomResponse> addGroupMember(@PathVariable Long roomId, @RequestBody AddMembersGroupRequest request) throws BaseException {
        return groupService.addGroupMember(roomId, request);
    }

    @PostMapping("/{roomId}/members/{userId}/kick")
    public ApiResponse<RoomResponse> kickGroupMember(@PathVariable Long roomId, @PathVariable Long userId) throws BaseException {
        return groupService.kickGroupMember(roomId, userId);
    }

    @DeleteMapping("/{roomId}")
    public ApiResponse<Object> deleteGroup(@PathVariable Long roomId) throws BaseException {
        return groupService.deleteGroup(roomId);
    }
}
