package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.LoginRequest;
import com.natsukashiiz.sbchat.model.request.SignupRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.TokenResponse;
import com.natsukashiiz.sbchat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) throws BaseException {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public ApiResponse<TokenResponse> signup(@RequestBody SignupRequest request) throws BaseException {
        return authService.signup(request);
    }
}
