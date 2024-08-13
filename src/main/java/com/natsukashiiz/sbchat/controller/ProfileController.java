package com.natsukashiiz.sbchat.controller;

import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.UpdateProfileRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.UserResponse;
import com.natsukashiiz.sbchat.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApiResponse<UserResponse> getProfile() throws BaseException {
        return profileService.getProfile();
    }

    @PutMapping
    public ApiResponse<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) throws BaseException {
        return profileService.updateProfile(request);
    }
}
