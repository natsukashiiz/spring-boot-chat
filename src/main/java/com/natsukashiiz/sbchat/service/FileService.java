package com.natsukashiiz.sbchat.service;


import com.natsukashiiz.sbchat.common.FileType;
import com.natsukashiiz.sbchat.entity.File;
import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.exception.FileException;
import com.natsukashiiz.sbchat.model.request.UploadFileRequest;
import com.natsukashiiz.sbchat.model.response.FileLoadAsResourceResponse;
import com.natsukashiiz.sbchat.model.response.UploadFileResponse;
import com.natsukashiiz.sbchat.repository.FileRepository;
import com.natsukashiiz.sbchat.utils.RandomUtils;
import com.natsukashiiz.sbchat.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileService {

    @Value("#{T(java.nio.file.Paths).get('${api.files.directory}')}")
    private Path fileDirectory;

    @Value("${api.files.supported-types}")
    private List<String> supportedFileTypes;

    @Value("${api.files.base-url}")
    private String fileBaseUrl;

    private final FileRepository fileRepository;
    private final AuthService authService;

    public ResponseEntity<?> upload(UploadFileRequest request) throws BaseException {
        var user = authService.getUser();
        var file = request.getFile();
        try {
            if (file.isEmpty()) {
                log.warn("Upload-[block]:(file is empty). file:{}", file);
                throw FileException.empty();
            }
            if (!StringUtils.hasText(file.getContentType())) {
                log.warn("Upload-[block]:(file content type is empty). file:{}", file);
                throw FileException.typeNotSupported();
            }
            if (!supportedFileTypes.contains(file.getContentType())) {
                log.warn("Upload-[block]:(file content type not supported). file:{}", file);
                throw FileException.typeNotSupported();
            }
            if (!Files.exists(fileDirectory)) {
                log.debug("Upload-[next]:(create directory). fileDirectory:{}", fileDirectory);
                Files.createDirectories(fileDirectory);
            }

            var format = file.getContentType().split("/")[1];
            var newFileName = RandomUtils.notSymbol() + "." + format;

            Path destinationFile = fileDirectory.resolve(Paths.get(newFileName)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(fileDirectory.toAbsolutePath())) {
                log.warn("Upload-[block]:(invalid path). destinationFile:{}", destinationFile);
                throw FileException.unknown();
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            var url = fileBaseUrl + newFileName;
            var fileType = getFileType(file.getContentType());

            var fileEntity = new File();
            fileEntity.setUser(user);
            fileEntity.setName(newFileName);
            fileEntity.setPath(destinationFile.toString());
            fileEntity.setUrl(url);
            fileEntity.setType(fileType);
            fileEntity.setContentType(file.getContentType());
            fileEntity.setSize(file.getSize());
            fileEntity.setFormat(format);
            fileEntity.setIsPublic(request.isPublic());
            fileEntity.setDeletedAt(null);
            fileRepository.save(fileEntity);

            var response = new UploadFileResponse();
            response.setName(newFileName);
            response.setUrl(url);
            response.setType(fileType);
            response.setFormat(format);
            response.setSize(file.getSize());

            return ResponseUtils.success(response);
        } catch (IOException e) {
            log.warn("Upload-[unknown].", e);
            throw FileException.unknown();
        }
    }

    private FileType getFileType(String contentType) {
        if (contentType.startsWith("image")) {
            return FileType.Image;
        } else {
            return FileType.Other;
        }
    }

    public Path loadFile(String fileName) {
        return fileDirectory.resolve(fileName);
    }

    public FileLoadAsResourceResponse loadAsResource(String fileName) throws BaseException {

        if (!StringUtils.hasText(fileName)) {
            log.warn("LoadAsResource-[block]:(file name is empty). fileName:{}", fileName);
            throw FileException.notFound();
        }

        var fileOptional = fileRepository.findByName(fileName);
        if (fileOptional.isEmpty()) {
            log.warn("LoadAsResource-[block]:(file not found in table). fileName:{}", fileName);
            throw FileException.notFound();
        }

        var file = fileOptional.get();

        if (Objects.nonNull(file.getDeletedAt())) {
            log.warn("LoadAsResource-[block]:(file is deleted). fileName:{}", fileName);
            throw FileException.notFound();
        }

        try {
            var path = loadFile(fileName);
            var resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                var response = new FileLoadAsResourceResponse();
                response.setFile(file);
                response.setResource(resource);
                return response;
            } else {
                throw FileException.notFound();
            }
        } catch (MalformedURLException e) {
            log.warn("LoadAsResource-[unknown].", e);
            throw FileException.unknown();
        }
    }

    public void deleteByFileName(String fileName) throws BaseException {
        try {
            var file = loadAsResource(fileName).getResource().getFile().toPath();
            Files.delete(file);
        } catch (IOException e) {
            log.warn("Delete-[unknown].", e);
            throw FileException.unknown();
        }
    }

    public ResponseEntity<?> deleteByUrl(String url) throws BaseException {
        var user = authService.getUser();

        if (!StringUtils.hasText(url)) {
            log.warn("deleteByUrl-[block]:(url is empty). userId:{}, url:{}.", user.getId(), url);
            throw FileException.invalidUrl();
        }

        var fileOptional = fileRepository.findByUrl(url);
        if (fileOptional.isEmpty()) {
            log.warn("deleteByUrl-[block]:(file not found in table). userId:{}, url:{}.", user.getId(), url);
            throw FileException.notFound();
        }

        var file = fileOptional.get();
        if (!Objects.equals(file.getUser().getId(), user.getId())) {
            log.warn("deleteByUrl-[block]:(file not belong to user). userId:{}, url:{}.", user.getId(), url);
            throw FileException.notFound();
        }

        if (Objects.nonNull(file.getDeletedAt())) {
            log.warn("deleteByUrl-[block]:(file is deleted). userId:{}, url:{}.", user.getId(), url);
            throw FileException.notFound();
        }

        try {
            deleteByFileName(file.getName());

            file.setDeletedAt(LocalDateTime.now());
            fileRepository.save(file);

            return ResponseUtils.success();
        } catch (Exception e) {
            log.warn("deleteByUrl-[unknown]. userId:{}, url:{}.", user.getId(), url, e);
            throw FileException.unknown();
        }
    }
}
