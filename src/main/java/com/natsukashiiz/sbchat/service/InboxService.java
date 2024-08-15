package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.Inbox;
import com.natsukashiiz.sbchat.entity.Message;
import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.response.*;
import com.natsukashiiz.sbchat.repository.InboxRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class InboxService {

    private final AuthService authService;
    private final InboxRepository inboxRepository;

    public ApiResponse<List<InboxResponse>> getInboxes() throws BaseException {
        var user = authService.getUser();

        var responses = inboxRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::createInboxResponse)
                .toList();

        return ResponseUtils.successList(responses);
    }

    private InboxResponse createInboxResponse(Inbox inbox) {
        var room = inbox.getRoom();
        var unreadCount = inbox.getUnreadCount();

        var roomResponse = new RoomResponse();
        roomResponse.setId(room.getId());
        roomResponse.setType(room.getType());
        roomResponse.setName(room.getName());
        roomResponse.setImage(room.getImage());

        var members = room.getMembers();
        if (room.getType() == RoomType.Friend) {
            var friend = members.stream()
                    .filter(m -> !m.getUser().getId().equals(inbox.getUser().getId()))
                    .findFirst()
                    .orElseThrow();
            roomResponse.setFriend(createUserResponse(friend.getUser()));
            roomResponse.setName(friend.getUser().getNickname());
            roomResponse.setImage(friend.getUser().getAvatar());
        } else {
            roomResponse.setMembers(members.stream()
                    .map(m -> createMemberResponse(m.getUser(), room.getOwner()))
                    .toList());
        }

        var response = new InboxResponse();
        response.setId(inbox.getId());
        response.setUnreadCount(unreadCount);

        if (Objects.nonNull(inbox.getLastMessage())) {
            var lastMessage = inbox.getLastMessage();
            var lastMessageResponse = createMessageResponse(lastMessage);

            if (Objects.nonNull(lastMessage.getReplyTo())) {
                var replyToResponse = createMessageResponse(lastMessage.getReplyTo());
                lastMessageResponse.setReplyTo(replyToResponse);
            }

            response.setLastMessage(lastMessageResponse);
        }

        response.setRoom(roomResponse);

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
        var sender = message.getSender();
        var senderResponse = createUserResponse(sender);

        var response = new MessageResponse();
        response.setId(message.getId());
        response.setAction(message.getAction());
        response.setType(message.getType());
        response.setContent(message.getContent());
        response.setSender(senderResponse);
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
