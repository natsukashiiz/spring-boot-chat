package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.LoginRequest;
import com.natsukashiiz.sbchat.model.request.SignupRequest;
import com.natsukashiiz.sbchat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws BaseException {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) throws BaseException {
        return authService.signup(request);
    }
}
