package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.common.MessageAction;
import com.natsukashiiz.sbchat.common.Pagination;
import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Message;
import com.natsukashiiz.sbchat.entity.Room;
import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.MessageException;
import com.natsukashiiz.sbchat.model.request.ReplyMessageRequest;
import com.natsukashiiz.sbchat.model.request.SendMessageRequest;
import com.natsukashiiz.sbchat.model.response.*;
import com.natsukashiiz.sbchat.repository.*;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ApiResponse<RoomResponse> getMessages(Long roomId, Pagination pagination) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("SendMessage-[block]:(not member). userId:{}, roomId:{}.", user.getId(), roomId);
                    return MessageException.notMember();
                });

        readAllMessages(roomId, user.getId());

        var inbox = inboxRepository.findByRoomIdAndUserId(roomId, user.getId()).get();
        var messagesPage = messageRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pagination);
        var responses = createRoomResponse(
                roomMember.getRoom(),
                user.getId(),
                messagesPage.getContent(),
                messagesPage.getTotalElements(),
                inbox.getUnreadCount()
        );

        return ResponseUtils.success(responses);
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
        message.setAction(MessageAction.SendMessage);
        message.setType(request.getType());
        message.setContent(request.getContent());
        messageRepository.save(message);

        var inboxOfMembers = inboxRepository.findAllByRoomId(roomId);
        for (var inbox : inboxOfMembers) {

            if (!Objects.equals(user.getId(), inbox.getUser().getId())) {
                inbox.setUnreadCount(inbox.getUnreadCount() + 1);
            }

            inbox.setLastMessage(message);
            inboxRepository.save(inbox);
        }

        var response = createMessageResponse(message);
        var roomResponse = new RoomResponse();
        roomResponse.setId(room.getId());
        response.setRoom(roomResponse);

        // ส่งข้อความไปยังห้องที่กำหนด
        for (var member : room.getMembers()) {
            if (Objects.equals(member.getUser().getId(), user.getId())) {
                continue;
            }

            if (Objects.equals(member.getMuted(), Boolean.FALSE)) {
                log.info("SendMessage:[next]. userId:{}, memberId:{}, message:{}", user.getId(), member.getUser().getId(), message);
                messagingTemplate.convertAndSendToUser(member.getUser().getId().toString(), "/topic/messages", response);
            }
        }

        readAllMessages(roomId, user.getId());
        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<MessageResponse> replyMessage(Long roomId, ReplyMessageRequest request) throws BaseException {
        var user = authService.getUser();

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> {
                    log.warn("ReplyMessage-[block]:(not member). userId:{}, roomId:{}, request:{}", user.getId(), roomId, request);
                    return MessageException.notMember();
                });

        var room = roomMember.getRoom();

        var replyTo = messageRepository.findById(request.getReplyToId())
                .orElseThrow(() -> {
                    log.warn("ReplyMessage-[block]:(not found). userId:{}, roomId:{}, request:{}", user.getId(), roomId, request);
                    return MessageException.notFound();
                });

        var message = new Message();
        message.setRoom(room);
        message.setSender(user);
        message.setAction(MessageAction.ReplyMessage);
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setReplyTo(replyTo);
        messageRepository.save(message);

        var inboxOfMembers = inboxRepository.findAllByRoomId(roomId);
        for (var inbox : inboxOfMembers) {
            if (!Objects.equals(user.getId(), inbox.getUser().getId())) {
                inbox.setUnreadCount(inbox.getUnreadCount() + 1);
            }

            inbox.setLastMessage(message);
            inboxRepository.save(inbox);
        }

        var response = createMessageResponse(message);
        var roomResponse = new RoomResponse();
        roomResponse.setId(room.getId());
        response.setRoom(roomResponse);

        // ส่งข้อความไปยังห้องที่กำหนด
        for (var member : room.getMembers()) {
            if (Objects.equals(member.getUser().getId(), user.getId())) {
                continue;
            }

            if (Objects.equals(member.getMuted(), Boolean.FALSE)) {
                log.info("ReplyMessage:[next]. userId:{}, memberId:{}, message:{}", user.getId(), member.getUser().getId(), message);
                messagingTemplate.convertAndSendToUser(member.getUser().getId().toString(), "/topic/messages", response);
            }
        }

        readAllMessages(roomId, user.getId());
        return ResponseUtils.success(response);
    }

    public void readAllMessages(Long roomId, Long userId) throws BaseException {
        var inbox = inboxRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> {
                    log.warn("ReadAllMessages-[block]:(not found inbox). userId:{}, roomId:{}.", userId, roomId);
                    return MessageException.notFound();
                });

        inbox.setUnreadCount(0);
        inboxRepository.save(inbox);
    }

    @Transactional
    public void typingMessage(TypingMessage typingMessage, Principal principal) throws BaseException {
        var user = userRepository.findById(Long.parseLong(principal.getName()))
                .orElseThrow(() -> {
                    log.warn("TypingMessage-[block]:(not found). username:{}.", principal.getName());
                    return MessageException.notFound();
                });

        var roomMember = roomMemberRepository.findByRoomIdAndUserId(typingMessage.getRoomId(), user.getId())
                .orElseThrow(() -> {
                    log.warn("TypingMessage-[block]:(not member). userId:{}, roomId:{}.", user.getId(), typingMessage.getRoomId());
                    return MessageException.notMember();
                });

        typingMessage.setUser(createUserResponse(user));
        var room = roomMember.getRoom();
        for (var member : room.getMembers()) {
            if (Objects.equals(member.getUser().getId(), user.getId())) {
                continue;
            }
            log.info("TypingMessage:[next]. userId:{}, memberId:{}, message:{}", user.getId(), member.getUser().getId(), typingMessage);
            messagingTemplate.convertAndSendToUser(member.getUser().getId().toString(), "/topic/typing", typingMessage);
        }
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
}
