package com.souha.securefilesharingplatform.service.impl;

import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.FileShare;
import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.repository.FileRepository;
import com.souha.securefilesharingplatform.repository.FileShareRepository;
import com.souha.securefilesharingplatform.repository.UserRepository;
import com.souha.securefilesharingplatform.service.FileService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileShareRepository fileShareRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FileServiceImpl(
            FileRepository fileRepository,
            UserRepository userRepository,
            FileShareRepository fileShareRepository
    ) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileShareRepository = fileShareRepository;
    }

    @Override
    public File uploadFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();

            String storedFilename =
                    UUID.randomUUID() + "_" + originalFilename;

            Path filePath = uploadPath.resolve(storedFilename);

            Files.copy(file.getInputStream(), filePath);

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String email = authentication.getName();

            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            File fileEntity = new File();

            fileEntity.setOriginalFilename(originalFilename);
            fileEntity.setStoredFilename(storedFilename);
            fileEntity.setContentType(file.getContentType());
            fileEntity.setSize(file.getSize());
            fileEntity.setStoragePath(filePath.toString());
            fileEntity.setCreatedAt(LocalDateTime.now());
            fileEntity.setOwner(owner);

            return fileRepository.save(fileEntity);

        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    @Override
    public List<File> getMyFiles() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return fileRepository.findByOwner(owner);
    }

    @Override
    public Resource downloadFile(Long fileId) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        boolean isOwner =
                file.getOwner().getId().equals(currentUser.getId());

        boolean isShared =
                fileShareRepository.existsByFileIdAndSharedWithId(
                        fileId,
                        currentUser.getId()
                );

        if (!isOwner && !isShared) {
            throw new RuntimeException(
                    "You don't have access to this file"
            );
        }

        Path path = Paths.get(file.getStoragePath());

        try {
            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException(
                        "File cannot be read"
                );
            }

            return resource;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not read file",
                    e
            );
        }
    }

    @Override
    public void deleteFile(Long fileId) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException(
                    "You don't have permission to delete this file"
            );
        }

        try {
            Path path = Paths.get(file.getStoragePath());

            Files.deleteIfExists(path);

            fileShareRepository.deleteAll(
                    fileShareRepository.findByFile(file)
            );

            fileRepository.delete(file);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not delete file",
                    e
            );
        }
    }

    @Override
    public void shareFile(Long fileId, String email) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String ownerEmail = authentication.getName();

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Owner not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException(
                    "You don't have permission to share this file"
            );
        }

        User sharedWith = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User to share with not found"
                        ));

        if (fileShareRepository.existsByFileIdAndSharedWithId(
                fileId,
                sharedWith.getId()
        )) {
            throw new RuntimeException(
                    "File is already shared with this user"
            );
        }

        FileShare fileShare = new FileShare();

        fileShare.setFile(file);
        fileShare.setSharedWith(sharedWith);
        fileShare.setSharedAt(LocalDateTime.now());

        fileShareRepository.save(fileShare);
    }

    @Override
    public List<File> getSharedFiles() {

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

        return fileShareRepository.findBySharedWith(user)
            .stream()
            .map(FileShare::getFile)
            .toList();
    }
}