package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.MessageAction;
import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.*;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.GroupException;
import com.natsukashiiz.sbchat.model.request.AddMembersGroupRequest;
import com.natsukashiiz.sbchat.model.request.UpdateGroupPhotoRequest;
import com.natsukashiiz.sbchat.model.request.CreateGroupRequest;
import com.natsukashiiz.sbchat.model.request.RenameGroupRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.MemberGroupResponse;
import com.natsukashiiz.sbchat.model.response.RoomResponse;
import com.natsukashiiz.sbchat.repository.*;
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
    private final MessageRepository messageRepository;

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

    @Transactional
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
        roomRepository.save(roomEntity);

        var lastMessage = new Message();
        lastMessage.setSender(user);
        lastMessage.setRoom(roomEntity);
        lastMessage.setAction(MessageAction.CreateGroupChat);
        messageRepository.save(lastMessage);

        request.getMemberIds().add(user.getId());
        for (var userId : request.getMemberIds()) {

            var member = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("CreateGroup-[block]:(user not found). userId:{}, memberId:{}", user.getId(), userId);
                        return GroupException.memberInvalid();
                    });

            var otherRoomMember = new RoomMember();
            otherRoomMember.setRoom(roomEntity);
            otherRoomMember.setUser(member);
            otherRoomMember.setMuted(false);
//            roomEntity.getMembers().add(otherRoomMember);
            roomMemberRepository.save(otherRoomMember);

            var otherInbox = new Inbox();
            otherInbox.setUser(member);
            otherInbox.setRoom(roomEntity);
            otherInbox.setLastMessage(lastMessage);
            otherInbox.setUnreadCount(1);
            inboxRepository.save(otherInbox);
        }

        var response = createGroupRoomResponse(roomEntity);
        return ResponseUtils.success(response);
    }

    public ApiResponse<RoomResponse> updateGroupName(Long roomId, RenameGroupRequest request) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("UpdateGroupName-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("UpdateGroupName-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        if (!StringUtils.hasText(request.getName())) {
            log.warn("UpdateGroupName-[block]:(invalid name). userId:{}, request:{}", user.getId(), request);
            throw GroupException.nameInvalid();
        }

        room.setName(request.getName());
        roomRepository.save(room);

        var message = new Message();
        message.setAction(MessageAction.RenameGroupChat);
        message.setRoom(room);
        message.setSender(user);
        message.setContent(request.getName());
        messageRepository.save(message);

        var response = createGroupRoomResponse(room);
        return ResponseUtils.success(response);
    }

    public ApiResponse<RoomResponse> updateGroupPhoto(Long roomId, UpdateGroupPhotoRequest request) throws BaseException {
        var user = authService.getUser();
        var room = roomRepository.findByIdAndMembersUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("UpdateGroupPhoto-[block]:(room not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return GroupException.notFound();
                });

        if (!Objects.equals(user.getId(), room.getOwner().getId())) {
            log.warn("UpdateGroupPhoto-[block]:(not permission). userId:{}, roomId:{}", user.getId(), roomId);
            throw GroupException.notPermission();
        }

        room.setImage(request.getPhoto());
        roomRepository.save(room);

        var message = new Message();
        message.setAction(MessageAction.ChangeGroupChatPhoto);
        message.setRoom(room);
        message.setSender(user);
        message.setContent(request.getPhoto());
        messageRepository.save(message);

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
            roomMemberRepository.save(roomMember);

            var message = new Message();
            message.setAction(MessageAction.AddGroupMember);
            message.setRoom(room);
            message.setSender(user);
            message.setMention(member);
            messageRepository.save(message);
        }

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

        var message = new Message();
        message.setAction(MessageAction.RemoveGroupMember);
        message.setRoom(room);
        message.setSender(user);
        message.setMention(member.getUser());
        messageRepository.save(message);

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
