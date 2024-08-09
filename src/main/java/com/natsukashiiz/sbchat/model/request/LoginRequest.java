package com.natsukashiiz.sbchat.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    @ToString.Exclude
    private String password;
}
