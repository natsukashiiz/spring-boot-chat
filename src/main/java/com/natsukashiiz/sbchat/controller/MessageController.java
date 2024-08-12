package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.SendMessageRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.MessageResponse;
import com.natsukashiiz.sbchat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{roomId}")
    public ApiResponse<List<MessageResponse>> getMessages(@PathVariable Long roomId) throws BaseException {
        return messageService.getMessages(roomId);
    }

    @PostMapping("/{roomId}/send")
    public ApiResponse<MessageResponse> sendMessage(@PathVariable Long roomId, @RequestBody SendMessageRequest request) throws BaseException {
        return messageService.sendMessage(roomId, request);
    }
}
