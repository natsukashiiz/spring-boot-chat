package com.natsukashiiz.sbchat.model.response;

import com.natsukashiiz.sbchat.common.FileType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UploadFileResponse {

    private String name;
    private String url;
    private FileType type;
    private String format;
    private long size;
}
