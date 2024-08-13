package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Inbox;
import com.natsukashiiz.sbchat.entity.Room;
import com.natsukashiiz.sbchat.entity.RoomMember;
import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.GroupException;
import com.natsukashiiz.sbchat.model.request.AddMembersGroupRequest;
import com.natsukashiiz.sbchat.model.request.CreateGroupRequest;
import com.natsukashiiz.sbchat.model.request.UpdateGroupRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.MemberGroupResponse;
import com.natsukashiiz.sbchat.model.response.RoomResponse;
import com.natsukashiiz.sbchat.repository.InboxRepository;
import com.natsukashiiz.sbchat.repository.RoomMemberRepository;
import com.natsukashiiz.sbchat.repository.RoomRepository;
import com.natsukashiiz.sbchat.repository.UserRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class GroupService {

    private final AuthService authService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final InboxRepository inboxRepository;

    public ApiResponse<List<RoomResponse>> getGroups() throws BaseException {
        var user = authService.getUser();
        var rooms = roomRepository.findByMembersUserIdAndTypeOrderByCreatedAtDesc(user.getId(), RoomType.Group);
        var response = rooms.stream()
                .map(this::createGroupRoomResponse)
                .toList();
        return ResponseUtils.successList(response);
    }

    public ApiResponse<RoomResponse> getGroup(Long roomId) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("GetGroup-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });
        var response = createGroupRoomResponse(room);
        return ResponseUtils.success(response);
    }

    public ApiResponse<RoomResponse> createGroup(CreateGroupRequest request) throws BaseException {
        var user = authService.getUser();

        if (!StringUtils.hasText(request.getName())) {
            log.warn("CreateGroup-[block]:(invalid name). userId:{}, request:{}", user.getId(), request);
            throw GroupException.nameInvalid();
        }

        var roomEntity = new Room();
        roomEntity.setOwner(user);
        roomEntity.setType(RoomType.Group);
        roomEntity.setName(request.getName());
        roomEntity.setImage(request.getImage());

        var selfRoomMember = new RoomMember();
        selfRoomMember.setRoom(roomEntity);
        selfRoomMember.setUser(user);
        selfRoomMember.setMuted(false);
        roomEntity.getMembers().add(selfRoomMember);

        var selfInbox = new Inbox();
        selfInbox.setUser(user);
        selfInbox.setRoom(roomEntity);
        selfInbox.setUnreadCount(1);
        // continue code

        for (var userId : request.getMemberIds()) {
            if (Objects.equals(userId, user.getId())) {
                continue;
            }

            var member = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("CreateGroup-[block]:(user not found). userId:{}, memberId:{}", user.getId(), userId);
                        return GroupException.memberInvalid();
                    });

            var otherRoomMember = new RoomMember();
            otherRoomMember.setRoom(roomEntity);
            otherRoomMember.setUser(member);
            otherRoomMember.setMuted(false);
            roomEntity.getMembers().add(otherRoomMember);

            // TODO: Update Inbox Last Message
        }

        roomRepository.save(roomEntity);
        var response = createGroupRoomResponse(roomEntity);
        return ResponseUtils.success(response);
    }

    public ApiResponse<RoomResponse> updateGroup(Long roomId, UpdateGroupRequest request) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("UpdateGroup-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("UpdateGroup-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        if (!StringUtils.hasText(request.getName())) {
            log.warn("UpdateGroup-[block]:(invalid name). userId:{}, request:{}", user.getId(), request);
            throw GroupException.nameInvalid();
        }

        room.setName(request.getName());
        room.setImage(request.getImage());
        roomRepository.save(room);

        var response = createGroupRoomResponse(room);
        return ResponseUtils.success(response);
    }

    public ApiResponse<RoomResponse> addGroupMember(Long roomId, AddMembersGroupRequest request) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("AddGroupMember-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("AddGroupMember-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        for (var userId : request.getMemberIds()) {
            var member = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("AddGroupMember-[block]:(user not found). userId:{}, memberId:{}", user.getId(), userId);
                        return GroupException.memberInvalid();
                    });

            var roomMember = new RoomMember();
            roomMember.setRoom(room);
            roomMember.setUser(member);
            roomMember.setMuted(false);
            room.getMembers().add(roomMember);
        }

        roomRepository.save(room);
        var response = createGroupRoomResponse(room);
        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<RoomResponse> kickGroupMember(Long roomId, Long memberId) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("KickGroupMember-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("KickGroupMember-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        var member = room.getMembers().stream()
                .filter(it -> Objects.equals(it.getUser().getId(), memberId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("KickGroupMember-[block]:(member not found). userId:{}, memberId:{}", user.getId(), memberId);
                    return GroupException.memberInvalid();
                });

        room.getMembers().remove(member);
        roomMemberRepository.softDeleteByRoomIdAndUserId(roomId, memberId);

        var response = createGroupRoomResponse(room);
        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<Object> deleteGroup(Long roomId) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("DeleteGroup-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("DeleteGroup-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        roomRepository.softDeleteById(room.getId());
        return ResponseUtils.success();
    }

    private RoomResponse createGroupRoomResponse(Room room) {
        var response = new RoomResponse();
        response.setId(room.getId());
        response.setType(room.getType());
        response.setName(room.getName());
        response.setImage(room.getImage());
        response.setMembers(room.getMembers().stream()
                .map(RoomMember::getUser)
                .map(m -> createMemberResponse(m, room.getOwner()))
                .sorted((a, b) -> Boolean.compare(b.isOwner(), a.isOwner()))
                .toList());
        return response;
    }

    private MemberGroupResponse createMemberResponse(User user, User owner) {
        var response = new MemberGroupResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setMobile(user.getMobile());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setLastSeenAt(user.getLastSeenAt());
        response.setOwner(Objects.equals(user.getId(), owner.getId()));
        return response;
    }
}
