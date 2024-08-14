package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.common.Pagination;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.ReplyMessageRequest;
import com.natsukashiiz.sbchat.model.request.SendMessageRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.MessageResponse;
import com.natsukashiiz.sbchat.model.response.RoomResponse;
import com.natsukashiiz.sbchat.service.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getMessages(@PathVariable Long roomId, Pagination pagination) throws BaseException {
        return messageService.getMessages(roomId, pagination);
    }

    @PostMapping("/{roomId}/send")
    public ApiResponse<MessageResponse> sendMessage(@PathVariable Long roomId, @RequestBody SendMessageRequest request) throws BaseException {
        return messageService.sendMessage(roomId, request);
    }

    @PostMapping("/{roomId}/reply")
    public ApiResponse<MessageResponse> replyMessage(@PathVariable Long roomId, @RequestBody ReplyMessageRequest request) throws BaseException {
        return messageService.replyMessage(roomId, request);
    }
}
