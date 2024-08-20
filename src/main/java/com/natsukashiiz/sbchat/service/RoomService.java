package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Message;
import com.natsukashiiz.sbchat.entity.Room;
import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.RoomException;
import com.natsukashiiz.sbchat.model.response.*;
import com.natsukashiiz.sbchat.repository.RoomMemberRepository;
import com.natsukashiiz.sbchat.repository.RoomRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class RoomService {

    private final AuthService authService;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    public ApiResponse<Object> muteRoom(Long roomId) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("MuteRoom-[block]:(not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return RoomException.notFound();
                });

        roomMember.setMuted(true);
        roomMemberRepository.save(roomMember);

        return ResponseUtils.success();
    }

    public ApiResponse<Object> unmuteRoom(Long roomId) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("UnmuteRoom-[block]:(not found). userId:{}, roomId:{}", user.getId(), roomId);
                    return RoomException.notFound();
                });

        roomMember.setMuted(false);
        roomMemberRepository.save(roomMember);

        return ResponseUtils.success();
    }

    private RoomResponse createRoomResponse(Room room, Long senderId, List<Message> messages, Long messageCount, Integer unreadCount) {
        var response = new RoomResponse();
        response.setId(room.getId());
        response.setType(room.getType());

        if (room.getType() == RoomType.Friend) {
            var friend = room.getMembers().stream()
                    .filter(member -> !member.getUser().getId().equals(senderId))
                    .findFirst()
                    .orElseThrow();
            var user = friend.getUser();
            response.setName(user.getNickname());
            response.setImage(user.getAvatar());
        } else {
            response.setName(room.getName());
            response.setImage(room.getImage());

            var members = room.getMembers()
                    .stream()
                    .map(member -> createMemberResponse(member.getUser(), room.getOwner()))
                    .sorted((a, b) -> Boolean.compare(b.isOwner(), a.isOwner()))
                    .toList();
            response.setMembers(members);
        }

        response.setMessages(messages.stream()
                .map(this::createMessageResponse)
                .toList());
        response.setMessageCount(messageCount);
        response.setUnreadCount(unreadCount);

        return response;
    }

    private UserResponse createUserResponse(User user) {
        var response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setMobile(user.getMobile());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setLastSeenAt(user.getLastSeenAt());
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

    private MessageResponse createMessageResponse(Message message) {
        var response = new MessageResponse();
        response.setId(message.getId());
        response.setAction(message.getAction());
        response.setType(message.getType());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());

        if (Objects.nonNull(message.getSender())) {
            response.setSender(createUserResponse(message.getSender()));
        }

        if (Objects.nonNull(message.getMention())) {
            response.setMention(createUserResponse(message.getMention()));
        }

        if (Objects.nonNull(message.getReplyTo())) {
            response.setReplyTo(createMessageResponse(message.getReplyTo()));
        }

        return response;
    }
}
