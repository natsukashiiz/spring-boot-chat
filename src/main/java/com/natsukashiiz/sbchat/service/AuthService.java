package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.AuthException;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.LoginException;
import com.natsukashiiz.sbchat.exception.SignupException;
import com.natsukashiiz.sbchat.model.request.LoginRequest;
import com.natsukashiiz.sbchat.model.request.SignupRequest;
import com.natsukashiiz.sbchat.model.response.TokenResponse;
import com.natsukashiiz.sbchat.repository.UserRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import com.natsukashiiz.sbchat.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public ResponseEntity<?> login(LoginRequest request) throws BaseException {
        if (!StringUtils.hasText(request.getIdentifier())) {
            log.warn("Login-[block]:(identifier is empty)");
            throw LoginException.identifierInvalid();
        }

        var accountOptional = userRepository.findByUsernameOrMobile(request.getIdentifier(), request.getIdentifier());
        if (accountOptional.isEmpty()) {
            log.warn("Login-[block]:(not found account). identifier:{}", request.getIdentifier());
            throw LoginException.invalid();
        }

        var response = createTokenResponse(accountOptional.get());
        return ResponseUtils.success(response);
    }

    public ResponseEntity<?> signup(SignupRequest request) throws BaseException {
        if (ValidationUtils.invalidUsername(request.getUsername())) {
            log.warn("Signup-[block]:(invalid username)");
            throw SignupException.invalid();
        }

        if (ValidationUtils.invalidMobile(request.getMobile())) {
            log.warn("Signup-[block]:(invalid mobile)");
            throw SignupException.usernameInvalid();
        }

        if (!StringUtils.hasText(request.getPassword())) {
            log.warn("Signup-[block]:(password is empty)");
            throw SignupException.mobileInvalid();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Signup-[block]:(username is already exists). username:{}", request.getUsername());
            throw SignupException.usernameExists();
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            log.warn("Signup-[block]:(mobile is already exists). mobile:{}", request.getMobile());
            throw SignupException.mobileExists();
        }

        var account = new User();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(account);

        var response = createTokenResponse(account);
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
