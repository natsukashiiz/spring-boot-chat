package com.natsukashiiz.sbchat.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DeleteFileRequest {

    @NotBlank
    private String url;
}
