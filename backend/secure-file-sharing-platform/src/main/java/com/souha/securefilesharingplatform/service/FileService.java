package com.souha.securefilesharingplatform.service;

import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileService {

    File uploadFile(MultipartFile file);

    List<File> getMyFiles();

    Resource downloadFile(Long fileId);

    void deleteFile(Long fileId);

    void shareFile(Long fileId, String email);

    List<File> getSharedFiles();

    List<User> getFileShares(Long fileId);

    void revokeShare(Long fileId, Long userId);
}
