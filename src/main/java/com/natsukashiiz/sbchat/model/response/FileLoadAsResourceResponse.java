package com.natsukashiiz.sbchat.model.response;

import com.natsukashiiz.sbchat.entity.File;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.UrlResource;

@Setter
@Getter
public class FileLoadAsResourceResponse {

    private File file;
    private UrlResource resource;
}
