package com.souha.securefilesharingplatform.controller;

import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<File> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        File uploadedFile = fileService.uploadFile(file);

        return ResponseEntity.ok(uploadedFile);
    }

    

    @GetMapping("/myFiles")
    public ResponseEntity<List<File>> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {

    Resource resource = fileService.downloadFile(fileId);

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resource.getFilename() + "\""
            )
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);

        return ResponseEntity.ok("File deleted successfully");
    }

    @PostMapping("/{fileId}/share")
    public ResponseEntity<String> shareFile(@PathVariable Long fileId, @RequestParam String email) {
        fileService.shareFile(fileId, email);

        return ResponseEntity.ok(
            "File shared successfully"
        );
    }

    @GetMapping("/shared")
    public ResponseEntity<List<File>> getSharedFiles() {
        return ResponseEntity.ok(fileService.getSharedFiles());
    }

    @GetMapping("/{fileId}/shares")
    public ResponseEntity<List<User>> getFileShares(
        @PathVariable Long fileId
) {
    return ResponseEntity.ok(
            fileService.getFileShares(fileId)
    );
}

    @DeleteMapping("/{fileId}/share/{userId}")
    public ResponseEntity<String> revokeShare(
        @PathVariable Long fileId,
        @PathVariable Long userId
) {
    fileService.revokeShare(fileId, userId);

    return ResponseEntity.ok(
            "File sharing revoked successfully"
    );
}
}