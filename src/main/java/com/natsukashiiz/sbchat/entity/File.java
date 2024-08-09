package com.natsukashiiz.sbchat.entity;

import com.natsukashiiz.sbchat.common.FileType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(name = "files")
public class File extends BaseEntity {

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private FileType type;

    private String name;
    private String path;
    private String url;
    private String format;
    private String contentType;
    private Long size;
    private Boolean isPublic;
}
