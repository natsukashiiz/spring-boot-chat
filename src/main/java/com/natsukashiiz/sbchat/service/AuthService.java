package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.AuthException;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.LoginException;
import com.natsukashiiz.sbchat.exception.SignupException;
import com.natsukashiiz.sbchat.model.request.LoginRequest;
import com.natsukashiiz.sbchat.model.request.SignupRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.TokenResponse;
import com.natsukashiiz.sbchat.repository.UserRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import com.natsukashiiz.sbchat.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public ApiResponse<TokenResponse> login(LoginRequest request) throws BaseException {
        if (!StringUtils.hasText(request.getIdentifier())) {
            log.warn("Login-[block]:(invalid identifier). request:{}", request);
            throw LoginException.identifierInvalid();
        }

        var userOptional = userRepository.findByUsernameOrMobile(request.getIdentifier(), request.getIdentifier());
        if (userOptional.isEmpty()) {
            log.warn("Login-[block]:(not found user). request:{}", request);
            throw LoginException.invalid();
        }

        var response = createTokenResponse(userOptional.get());
        return ResponseUtils.success(response);
    }

    public ApiResponse<TokenResponse> signup(SignupRequest request) throws BaseException {
        if (ValidationUtils.invalidUsername(request.getUsername())) {
            log.warn("Signup-[block]:(invalid username). request:{}", request);
            throw SignupException.usernameInvalid();
        }

        if (ValidationUtils.invalidMobile(request.getMobile())) {
            log.warn("Signup-[block]:(invalid mobile). request:{}", request);
            throw SignupException.mobileInvalid();
        }

        if (!StringUtils.hasText(request.getPassword())) {
            log.warn("Signup-[block]:(invalid password). request:{}", request);
            throw SignupException.passwordInvalid();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Signup-[block]:(username is already exists). request:{}", request);
            throw SignupException.usernameExists();
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            log.warn("Signup-[block]:(mobile is already exists). request:{}", request);
            throw SignupException.mobileExists();
        }

        var user = new User();
        user.setUsername(request.getUsername());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setLastSeenAt(LocalDateTime.now());

        userRepository.save(user);

        var response = createTokenResponse(user);
        return ResponseUtils.success(response);
    }

    public User getUser() throws BaseException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("GetUser-[block]:(authentication is null)");
            throw AuthException.unauthorized();
        }

        if (!authentication.isAuthenticated()) {
            log.warn("GetUser-[block]:(not authenticated)");
            throw AuthException.unauthorized();
        }

        var jwt = (Jwt) authentication.getCredentials();

        if (jwt == null) {
            log.warn("GetUser-[block]:(jwt is null)");
            throw AuthException.unauthorized();
        }

        if (!tokenService.isAccessToken(jwt)) {
            log.warn("GetUser-[block]:(not access token)");
            throw AuthException.unauthorized();
        }

        var accountId = authentication.getName();

        if (ObjectUtils.isEmpty(accountId)) {
            log.warn("GetUser-[block]:(accountId is empty)");
            throw AuthException.unauthorized();
        }

        var accountOptional = userRepository.findById(Long.parseLong(accountId));
        if (accountOptional.isEmpty()) {
            log.warn("GetUser-[block]:(not found account). accountId:{}", accountId);
            throw AuthException.unauthorized();
        }

        return accountOptional.get();
    }

    public boolean anonymous() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return ObjectUtils.isEmpty(authentication) || authentication.getPrincipal().equals("anonymousUser");
    }

    public boolean passwordNotMatch(String raw, String hash) {
        return !passwordEncoder.matches(raw, hash);
    }

    public TokenResponse createTokenResponse(User user) {

        var accessToken = tokenService.generateAccessToken(user.getId());
        var refreshToken = tokenService.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken);
    }
}
