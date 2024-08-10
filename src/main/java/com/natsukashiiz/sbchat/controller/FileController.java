package com.natsukashiiz.sbchat.controller;


import com.natsukashiiz.sbchat.exception.BaseException;
import com.natsukashiiz.sbchat.model.request.DeleteFileRequest;
import com.natsukashiiz.sbchat.model.request.UploadFileRequest;
import com.natsukashiiz.sbchat.model.response.ApiResponse;
import com.natsukashiiz.sbchat.model.response.UploadFileResponse;
import com.natsukashiiz.sbchat.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<UploadFileResponse> upload(@ModelAttribute UploadFileRequest request) throws BaseException {
        return fileService.upload(request);
    }


    @GetMapping("/{fileName}")
    public ResponseEntity<?> loadAsResource(@PathVariable String fileName) {
        try {
            var response = fileService.loadAsResource(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(response.getFile().getContentType()))
                    .body(response.getResource());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ApiResponse<Object> delete(@RequestBody DeleteFileRequest request) throws BaseException {
        return fileService.deleteByUrl(request.getUrl());
    }
}