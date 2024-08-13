package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.common.FriendStatus;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.FriendResponse;
import com.natsukashiiz.sbchat.service.FriendService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/friends")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ApiResponse<List<FriendResponse>> getFriends(@RequestParam FriendStatus status) throws BaseException {
        return friendService.getFriends(status);
    }

    @GetMapping("/search")
    public ApiResponse<FriendResponse> searchFriend(@RequestParam String identifier) throws BaseException {
        return friendService.searchFriend(identifier);
    }

    @PostMapping("/{friendId}/apply")
    public ApiResponse<FriendResponse> applyFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.applyFriend(friendId);
    }

    @PostMapping("/{friendId}/unapply")
    public ApiResponse<Object> unapplyFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.unapplyFriend(friendId);
    }

    @PostMapping("/{friendId}/accept")
    public ApiResponse<FriendResponse> acceptFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.acceptFriend(friendId);
    }

    @PostMapping("/{friendId}/reject")
    public ApiResponse<Object> rejectFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.rejectFriend(friendId);
    }

    @PostMapping("/{friendId}/block")
    public ApiResponse<FriendResponse> blockFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.blockFriend(friendId);
    }

    @PostMapping("/{friendId}/unblock")
    public ApiResponse<FriendResponse> unblockFriend(@PathVariable Long friendId) throws BaseException {
        return friendService.unblockFriend(friendId);
    }

    @PostMapping("/{friendId}/unfriend")
    public ApiResponse<Object> unfriend(@PathVariable Long friendId) throws BaseException {
        return friendService.unfriend(friendId);
    }
}
