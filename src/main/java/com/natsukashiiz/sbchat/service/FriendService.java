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
    private final RoomMemberRepository roomMemberRepository;
    private final MessageRepository messageRepository;

    public ApiResponse<List<FriendResponse>> getFriends(FriendStatus status) throws BaseException {
        var user = authService.getUser();


        List<FriendResponse> responses;
        if (status == FriendStatus.Apply) {
            responses = friendRepository.findAllByFriendIdAndStatus(user.getId(), status).stream().map(friend -> createFriendResponse(friend.getUser(), status)).toList();
        } else {
            responses = friendRepository.findAllByUserIdAndStatus(user.getId(), status).stream().map(friend -> createFriendResponse(friend.getFriend(), status)).toList();
        }

        return ResponseUtils.success(responses);
    }

    public ApiResponse<FriendResponse> searchFriend(String identifier) throws BaseException {
        var user = authService.getUser();

        var friend = userRepository.findByUsernameOrMobile(identifier, identifier).orElseThrow(() -> {
            log.warn("SearchFriend-[block]:(friend not found). userId:{}, identifier:{}", user.getId(), identifier);
            return FriendException.notFound();
        });

        var selfFriend = friendRepository.findByUserIdAndFriendId(user.getId(), friend.getId()).orElse(new Friend());
        var response = createFriendResponse(friend, selfFriend.getStatus());
        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<FriendResponse> applyFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        if (Objects.equals(user.getId(), friendId)) {
            log.warn("ApplyFriend-[block]:(apply to self). userId:{}, friendId:{}", user.getId(), friendId);
            throw FriendException.applyToSelf();
        }

        var friend = userRepository.findByIdAndDeletedAtIsNull(friendId).orElseThrow(() -> {
            log.warn("ApplyFriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        var applyFriendEntity = new Friend();
        var selfFriendOptional = friendRepository.findByUserIdAndFriendId(user.getId(), friendId);

        if (selfFriendOptional.isPresent()) {
            var selfFriend = selfFriendOptional.get();
            if (Objects.isNull(selfFriend.getDeletedAt())) {
                log.warn("ApplyFriend-[block]:(already applied from self). userId:{}, friendId:{}", user.getId(), friendId);
                throw FriendException.duplicate();
            } else {
                friendRepository.updateDeleteAtNullByUserIdAndFriendId(user.getId(), friendId);
                applyFriendEntity.setId(selfFriend.getId());
                applyFriendEntity.setVersion(selfFriend.getVersion());
            }
        }

        applyFriendEntity.setUser(user);
        applyFriendEntity.setFriend(friend);
        applyFriendEntity.setStatus(FriendStatus.Apply);
        friendRepository.save(applyFriendEntity);

        var response = createFriendResponse(applyFriendEntity.getFriend(), applyFriendEntity.getStatus());

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

        if (Objects.nonNull(friend.getDeletedAt())) {
            friendRepository.softDeleteByUserIdAndFriendId(user.getId(), friendId);
        } else {
            friendRepository.delete(friend);
        }

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

        var selfUser = friend.getFriend();
        var friendUser = friend.getUser();

        var roomEntity = new Room();

        var selfFriend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElse(new Friend());

        // เคยเป็นเพื่อนกัน
        if (Objects.nonNull(selfFriend.getDeletedAt())) {
            friendRepository.updateDeleteAtNullByUserIdAndFriendId(friendId, user.getId());
            friend.setDeletedAt(null);

            selfFriend.setStatus(FriendStatus.Friend);
            friendRepository.save(selfFriend);

            roomEntity = friend.getRoom();
        } else {
            roomEntity.setType(RoomType.Friend);
            roomRepository.save(roomEntity);

            selfFriend.setUser(selfUser);
            selfFriend.setFriend(friendUser);
            selfFriend.setRoom(roomEntity);
            selfFriend.setStatus(FriendStatus.Friend);
            friendRepository.save(selfFriend);

            var selfRoomMemberEntity = new RoomMember();
            selfRoomMemberEntity.setUser(selfUser);
            selfRoomMemberEntity.setRoom(roomEntity);
            selfRoomMemberEntity.setMuted(false);
            roomMemberRepository.save(selfRoomMemberEntity);

            var friendRoomMemberEntity = new RoomMember();
            friendRoomMemberEntity.setUser(friendUser);
            friendRoomMemberEntity.setRoom(roomEntity);
            friendRoomMemberEntity.setMuted(false);
            roomMemberRepository.save(friendRoomMemberEntity);
        }

        var selfMessageEntity = new Message();
        selfMessageEntity.setRoom(roomEntity);
        selfMessageEntity.setSender(selfUser);
        selfMessageEntity.setType(MessageType.Join);
        selfMessageEntity.setContent("You are now friends");
        messageRepository.save(selfMessageEntity);

        var selfInboxEntity = inboxRepository.findByRoomIdAndUserId(roomEntity.getId(), selfUser.getId()).orElse(new Inbox());
        selfInboxEntity.setRoom(roomEntity);
        selfInboxEntity.setUser(selfUser);
        selfInboxEntity.setLastMessage(selfMessageEntity);
        selfInboxEntity.setUnreadCount(1);
        inboxRepository.save(selfInboxEntity);

        var friendMessageEntity = new Message();
        friendMessageEntity.setRoom(roomEntity);
        friendMessageEntity.setSender(friendUser);
        friendMessageEntity.setType(MessageType.Join);
        friendMessageEntity.setContent("You are now friends");
        messageRepository.save(friendMessageEntity);

        var friendInboxEntity = inboxRepository.findByRoomIdAndUserId(roomEntity.getId(), friendUser.getId()).orElse(new Inbox());
        friendInboxEntity.setRoom(roomEntity);
        friendInboxEntity.setUser(friendUser);
        friendInboxEntity.setLastMessage(friendMessageEntity);
        friendInboxEntity.setUnreadCount(1);
        inboxRepository.save(friendInboxEntity);

        friend.setStatus(FriendStatus.Friend);
        friend.setRoom(roomEntity);
        friendRepository.save(friend);

        var response = createFriendResponse(friendUser, friend.getStatus());
        return ResponseUtils.success(response);
    }

    public ApiResponse<Object> rejectFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(friendId, user.getId()).orElseThrow(() -> {
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

    @Transactional
    public ApiResponse<FriendResponse> blockFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var selfFriend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("BlockFriend-[block]:(selfFriend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (selfFriend.getStatus() != FriendStatus.Friend) {
            log.warn("BlockFriend-[block]:(not selfFriend). userId:{}, friendId:{}, status:{}", user.getId(), friendId, selfFriend.getStatus());
            throw FriendException.notFriend();
        }

        selfFriend.setStatus(FriendStatus.BlockBySelf);
        friendRepository.save(selfFriend);

        var friendSelf = friendRepository.findByUserIdAndFriendId(friendId, user.getId()).get();
        friendSelf.setStatus(FriendStatus.BlockByFriend);
        friendRepository.save(friendSelf);

        var response = createFriendResponse(selfFriend.getFriend(), selfFriend.getStatus());

        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<FriendResponse> unblockFriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var selfFriend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("UnblockFriend-[block]:(selfFriend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (selfFriend.getStatus() != FriendStatus.BlockBySelf) {
            log.warn("UnblockFriend-[block]:(not blocked). userId:{}, friendId:{}, status:{}", user.getId(), friendId, selfFriend.getStatus());
            throw FriendException.notBlocked();
        }

        selfFriend.setStatus(FriendStatus.Friend);
        friendRepository.save(selfFriend);

        var friendSelf = friendRepository.findByUserIdAndFriendId(friendId, user.getId()).get();
        friendSelf.setStatus(FriendStatus.Friend);
        friendRepository.save(friendSelf);

        var response = createFriendResponse(selfFriend.getFriend(), selfFriend.getStatus());

        return ResponseUtils.success(response);
    }

    @Transactional
    public ApiResponse<Object> unfriend(Long friendId) throws BaseException {
        var user = authService.getUser();

        var friend = friendRepository.findByUserIdAndFriendId(user.getId(), friendId).orElseThrow(() -> {
            log.warn("Unfriend-[block]:(friend not found). userId:{}, friendId:{}", user.getId(), friendId);
            return FriendException.notFound();
        });

        if (Objects.nonNull(friend.getDeletedAt())) {
            log.warn("Unfriend-[block]:(not friend). userId:{}, friendId:{}, status:{}", user.getId(), friendId, friend.getStatus());
            throw FriendException.notFriend();
        }

        friendRepository.softDeleteByUserIdAndFriendId(user.getId(), friendId);
        friendRepository.softDeleteByUserIdAndFriendId(friendId, user.getId());

        return ResponseUtils.success();
    }

    private FriendResponse createFriendResponse(User friend, FriendStatus status) {
        var friendResponse = new UserResponse();
        friendResponse.setId(friend.getId());
        friendResponse.setUsername(friend.getUsername());
        friendResponse.setMobile(friend.getMobile());
        friendResponse.setNickname(friend.getNickname());
        friendResponse.setAvatar(friend.getAvatar());

        if (status == FriendStatus.Friend) {
            friendResponse.setLastSeenAt(friend.getLastSeenAt());
        }

        var response = new FriendResponse();
        response.setFriend(friendResponse);
        response.setStatus(status);
        return response;
    }
}
