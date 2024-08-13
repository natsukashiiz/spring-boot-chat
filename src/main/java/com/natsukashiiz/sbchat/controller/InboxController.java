package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.InboxResponse;
import com.natsukashiiz.sbchat.service.InboxService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/inboxes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InboxController {

    private final InboxService inboxService;

    @GetMapping
    public ApiResponse<List<InboxResponse>> getInboxes() throws BaseException {
        return inboxService.getInboxes();
    }
}
