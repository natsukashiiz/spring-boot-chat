package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.entity.Inbox;
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
        var lastMessage = inbox.getLastMessage();
        var unreadCount = inbox.getUnreadCount();

        var roomResponse = new RoomResponse();
        roomResponse.setId(room.getId());
        roomResponse.setType(room.getType());
        roomResponse.setName(room.getName());
        roomResponse.setImage(room.getImage());

        var owner = room.getOwner();
        if (Objects.nonNull(owner)) {
            var ownerResponse = createUserResponse(owner);
            roomResponse.setOwner(ownerResponse);
        }

        var lastMessageResponse = new MessageResponse();
        lastMessageResponse.setId(lastMessage.getId());
        lastMessageResponse.setType(lastMessage.getType());
        lastMessageResponse.setContent(lastMessage.getContent());

        var sender = lastMessage.getSender();
        var senderResponse = createUserResponse(sender);
        lastMessageResponse.setSender(senderResponse);

        var response = new InboxResponse();
        response.setId(inbox.getId());
        response.setUnreadCount(unreadCount);
        response.setLastMessage(lastMessageResponse);
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
}
