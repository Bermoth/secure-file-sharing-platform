package com.souha.securefilesharingplatform.repository;

import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.FileShare;
import com.souha.securefilesharingplatform.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileShareRepository
        extends JpaRepository<FileShare, Long> {

    List<FileShare> findBySharedWith(User user);

    List<FileShare> findByFile(File file);

    boolean existsByFileIdAndSharedWithId(
            Long fileId,
            Long userId
    );
}