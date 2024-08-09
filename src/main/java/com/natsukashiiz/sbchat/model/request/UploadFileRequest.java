package com.natsukashiiz.sbchat.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@ToString
public class UploadFileRequest {

    @NotNull
    private MultipartFile file;

    private boolean isPublic = true;
}
