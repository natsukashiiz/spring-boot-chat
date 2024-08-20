package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/{roomId}/mute")
    public ApiResponse<Object> muteRoom(@PathVariable Long roomId) throws BaseException {
        return roomService.muteRoom(roomId);
    }

    @PostMapping("/{roomId}/unmute")
    public ApiResponse<Object> unmuteRoom(@PathVariable Long roomId) throws BaseException {
        return roomService.unmuteRoom(roomId);
    }
}
