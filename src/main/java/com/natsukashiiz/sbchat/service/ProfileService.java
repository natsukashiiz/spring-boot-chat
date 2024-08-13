package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.entity.User;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.ProfileException;
import com.natsukashiiz.sbchat.model.request.ChangePasswordRequest;
import com.natsukashiiz.sbchat.model.request.UpdateProfileRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.UserResponse;
import com.natsukashiiz.sbchat.repository.UserRepository;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ApiResponse<UserResponse> getProfile() throws BaseException {
        var user = authService.getUser();
        var response = createUserResponse(user);
        return ResponseUtils.success(response);
    }

    public ApiResponse<UserResponse> updateProfile(UpdateProfileRequest request) throws BaseException {
        var user = authService.getUser();

        if (StringUtils.hasText(request.getUsername()) && !Objects.equals(request.getUsername(), user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("UpdateProfile-[block]:(username already exists). userId:{}, request:{}", user.getId(), request);
                throw ProfileException.usernameExists();
            }
        }

        if (StringUtils.hasText(request.getMobile()) && !Objects.equals(request.getMobile(), user.getMobile())) {
            if (userRepository.existsByMobile(request.getMobile())) {
                log.warn("UpdateProfile-[block]:(mobile already exists). userId:{}, request:{}", user.getId(), request);
                throw ProfileException.mobileExists();
            }
        }

        user.setUsername(request.getUsername());
        user.setMobile(request.getMobile());
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        userRepository.save(user);

        var response = createUserResponse(user);
        return ResponseUtils.success(response);
    }

    public ApiResponse<Object> changePassword(ChangePasswordRequest request) throws BaseException {
        var user = authService.getUser();
        if (!Objects.equals(request.getCur(), user.getPassword())) {
            log.warn("ChangePassword-[block]:(password not match). userId:{}", user.getId());
            throw ProfileException.passwordNotMatch();
        }

        user.setPassword(request.getLatest());
        userRepository.save(user);
        return ResponseUtils.success();
    }

    private UserResponse createUserResponse(User user) {
        var response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setMobile(user.getMobile());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        return response;
    }
}
