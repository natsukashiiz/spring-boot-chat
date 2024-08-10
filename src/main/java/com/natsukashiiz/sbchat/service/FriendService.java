package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.common.MessageType;
import com.natsukashiiz.sbchat.common.RoomType;
import com.natsukashiiz.sbchat.entity.*;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.FriendException;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.FriendResponse;
import com.natsukashiiz.sbchat.model.response.UserResponse;
import com.natsukashiiz.sbchat.repository.*;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final InboxRepository inboxRepository;
    private final RoomRepository roomRepository;

    public ApiResponse<List<FriendResponse>> getFriends(FriendStatus status) throws BaseException {
        var user = authService.getUser();
        var friends = friendRepository.findAllByUserIdOrFriendIdAndStatus(user.getId(), user.getId(), status);

        var response = friends.stream().map(this::createFriendResponse).toList();

        return ResponseUtils.success(response);
    }

    public ApiResponse<FriendResponse> applyFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        if (Objects.equals(user.getId(), friendId)) {
            log.warn("ApplyFriend-[block]:(apply to self). userId:{}, friendId:{}", user.getId(), friendId);
            throw FriendException.applyToSelf();
        }

        var friend = userRepository.findById(friendId).orElseThrow(() -> {
            log.warn("ApplyFriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friendRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())) {
            log.warn("ApplyFriend-[block]:(already applied from self). userId:{}, friendId:{}", user.getId(), friendId);
            throw FriendException.duplicate();
        }

        if (friendRepository.existsByUserIdAndFriendId(friend.getId(), user.getId())) {
            log.warn("ApplyFriend-[block]:(already applied from friend). userId:{}, friendId:{}", user.getId(), friendId);
            throw FriendException.duplicate();
        }

        var applyFriendEntity = new Friend();
        applyFriendEntity.setUser(user);
        applyFriendEntity.setFriend(friend);
        applyFriendEntity.setStatus(FriendStatus.Apply);

        friendRepository.save(applyFriendEntity);
        var response = createFriendResponse(applyFriendEntity);

        return ResponseUtils.success(response);
    }

    public ApiResponse<Object> unapplyFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("UnapplyFriend-[block]:(apply not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friend.getStatus() != FriendStatus.Apply) {
            log.warn("UnapplyFriend-[block]:(not apply). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notApply();
        }

        friendRepository.delete(friend);

        return ResponseUtils.success();
    }

    @Transactional
    public ApiResponse<FriendResponse> acceptFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(friendId, user.getId()).orElseThrow(() -> {
            log.warn("AcceptFriend-[block]:(apply not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friend.getStatus() != FriendStatus.Apply) {
            log.warn("AcceptFriend-[block]:(not apply). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notApply();
        }

        friend.setStatus(FriendStatus.Friend);
        friendRepository.save(friend);

        var roomEntity = new Room();
        roomEntity.setType(RoomType.Friend);

        // self
        {
            var selfRoomMemberEntity = new RoomMember();
            selfRoomMemberEntity.setUser(friend.getFriend());
            selfRoomMemberEntity.setRoom(roomEntity);
            selfRoomMemberEntity.setMuted(false);
            roomEntity.getMembers().add(selfRoomMemberEntity);

            var selfMessageEntity = new Message();
            selfMessageEntity.setRoom(roomEntity);
            selfMessageEntity.setSender(friend.getFriend());
            selfMessageEntity.setType(MessageType.Join);
            selfMessageEntity.setContent("You are now friends");
            roomEntity.getMembers().add(selfRoomMemberEntity);

            var selfInboxEntity = new Inbox();
            selfInboxEntity.setRoom(roomEntity);
            selfInboxEntity.setUser(friend.getFriend());
            selfInboxEntity.setLastMessage(selfMessageEntity);
            selfInboxEntity.setUnreadCount(1);
            inboxRepository.save(selfInboxEntity);
        }

        // friend
        {
            var friendRoomMemberEntity = new RoomMember();
            friendRoomMemberEntity.setUser(friend.getUser());
            friendRoomMemberEntity.setRoom(roomEntity);
            friendRoomMemberEntity.setMuted(false);
            roomEntity.getMembers().add(friendRoomMemberEntity);

            var friendMessageEntity = new Message();
            friendMessageEntity.setRoom(roomEntity);
            friendMessageEntity.setSender(friend.getUser());
            friendMessageEntity.setType(MessageType.Join);
            friendMessageEntity.setContent("You are now friends");
            roomEntity.getMembers().add(friendRoomMemberEntity);

            var friendInboxEntity = new Inbox();
            friendInboxEntity.setRoom(roomEntity);
            friendInboxEntity.setUser(friend.getUser());
            friendInboxEntity.setLastMessage(friendMessageEntity);
            friendInboxEntity.setUnreadCount(1);
            inboxRepository.save(friendInboxEntity);
        }

        roomRepository.save(roomEntity);

        var response = createFriendResponse(friend);
        return ResponseUtils.success(response);
    }

    public ApiResponse<Object> rejectFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("RejectFriend-[block]:(apply not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friend.getStatus() != FriendStatus.Apply) {
            log.warn("RejectFriend-[block]:(not apply). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notApply();
        }

        friendRepository.delete(friend);

        return ResponseUtils.success();
    }

    public ApiResponse<FriendResponse> blockFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("BlockFriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friend.getStatus() != FriendStatus.Friend) {
            log.warn("BlockFriend-[block]:(not friend). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notFriend();
        }

        friend.setStatus(FriendStatus.Blocked);
        friendRepository.save(friend);
        var response = createFriendResponse(friend);

        return ResponseUtils.success(response);
    }

    public ApiResponse<FriendResponse> unblockFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("UnblockFriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (friend.getStatus() != FriendStatus.Blocked) {
            log.warn("UnblockFriend-[block]:(not blocked). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notBlocked();
        }

        friend.setStatus(FriendStatus.Friend);
        friendRepository.save(friend);
        var response = createFriendResponse(friend);

        return ResponseUtils.success(response);
    }

    public ApiResponse<Object> unfriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("Unfriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        friendRepository.delete(friend);

        return ResponseUtils.success();
    }

    private FriendResponse createFriendResponse(Friend friend) {
        var friendResponse = new UserResponse();
        friendResponse.setId(friend.getFriend().getId());
        friendResponse.setUsername(friend.getFriend().getUsername());
        friendResponse.setMobile(friend.getFriend().getMobile());
        friendResponse.setNickname(friend.getFriend().getNickname());
        friendResponse.setAvatar(friend.getFriend().getAvatar());
        friendResponse.setLastSeenAt(friend.getFriend().getLastSeenAt());

        var response = new FriendResponse();
        response.setFriend(friendResponse);
        response.setStatus(friend.getStatus());
        return response;
    }
}
