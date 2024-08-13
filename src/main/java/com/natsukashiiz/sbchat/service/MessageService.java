package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Message;
import com.natsukashiiz.sbchat.entity.Room;
import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.MessageException;
import com.natsukashiiz.sbchat.model.request.SendMessageRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.MessageResponse;
import com.natsukashiiz.sbchat.model.response.RoomResponse;
import com.natsukashiiz.sbchat.model.response.UserResponse;
import com.natsukashiiz.sbchat.repository.FriendRepository;
import com.natsukashiiz.sbchat.repository.InboxRepository;
import com.natsukashiiz.sbchat.repository.MessageRepository;
import com.natsukashiiz.sbchat.repository.RoomMemberRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessageService {

    private final AuthService authService;
    private final MessageRepository messageRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final InboxRepository inboxRepository;
    private final FriendRepository friendRepository;

    public ApiResponse<List<MessageResponse>> getMessages(Long roomId) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("SendMessage-[block]:(not member). userId:{}, roomId:{}.", user.getId(), roomId);
                    return MessageException.notMember();
                });

        var room = roomMember.getRoom();

        var responses = room.getMessages()
                .stream()
                .map(this::createMessageResponse)
                .toList();

        return ResponseUtils.successList(responses);
    }

    @Transactional
    public ApiResponse<MessageResponse> sendMessage(Long roomId, SendMessageRequest request) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("SendMessage-[block]:(not member). userId:{}, roomId:{}, request:{}", user.getId(), roomId, request);
                    return MessageException.notMember();
                });

        var room = roomMember.getRoom();

        if (room.getType() == RoomType.Friend) {
            var friendId = room.getMembers().stream()
                    .filter(member -> !member.getUser().getId().equals(user.getId()))
                    .map(member -> member.getUser().getId())
                    .findFirst()
                    .orElseThrow();

            var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
                log.warn("SendMessage-[block]:(not friend). userId:{}, friendId:{}, request:{}", user.getId(), room.getId(), request);
                return MessageException.notFriend();
            });

            if (Objects.nonNull(friend.getDeletedAt())) {
                log.warn("SendMessage-[block]:(friend deleted). userId:{}, friendId:{}, request:{}", user.getId(), room.getId(), request);
                throw MessageException.notFriend();
            }

            if (friend.getStatus() != FriendStatus.Friend) {
                log.warn("SendMessage-[block]:(status not friend). userId:{}, friendId:{}, request:{}", user.getId(), room.getId(), request);
                throw MessageException.notFriend();
            }
        }

        var message = new Message();
        message.setRoom(room);
        message.setSender(user);
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);

        var inboxOfMembers = inboxRepository.findAllByRoomId(roomId);
        for (var inbox : inboxOfMembers) {

            if (!Objects.equals(user.getId(), inbox.getUser().getId())) {
                inbox.setUnreadCount(inbox.getUnreadCount() + 1);
            }

            inbox.setLastMessage(message);
            inboxRepository.save(inbox);
        }

        // ส่งข้อความไปยังห้องที่กำหนด

        var response = createMessageResponse(message);
        return ResponseUtils.success(response);
    }

    private MessageResponse createMessageResponse(Message message) {
        var room = message.getRoom();
        var sender = message.getSender();

        var roomResponse = createRoomResponse(room, sender.getId());
        var senderResponse = createUserResponse(sender);

        var response = new MessageResponse();
        response.setId(message.getId());
        response.setType(message.getType());
        response.setContent(message.getContent());
        response.setRoom(roomResponse);
        response.setSender(senderResponse);
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private RoomResponse createRoomResponse(Room room, Long senderId) {
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
        }

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
}
