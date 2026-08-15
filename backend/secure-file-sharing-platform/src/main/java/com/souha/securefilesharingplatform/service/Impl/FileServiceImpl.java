package com.souha.securefilesharingplatform.service.impl;

import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.FileShare;
import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.exception.ForbiddenException;
import com.souha.securefilesharingplatform.exception.ResourceNotFoundException;
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

        String messageFile = "File not Found";
        String messageUser = "User not Found";

    @Override
    public File uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException(
                        "File name cannot be empty"
                );
            }

            String storedFilename =
                    UUID.randomUUID() + "_" + originalFilename;

            Path filePath = uploadPath.resolve(storedFilename);

            Files.copy(file.getInputStream(), filePath);

            String email = authentication.getName();

            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    messageUser
                            ));

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
            throw new RuntimeException(
                    "Could not store file",
                    e
            );
        }
    }

    @Override
    public List<File> getMyFiles() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        return fileRepository.findByOwner(owner);
    }

    @Override
    public Resource downloadFile(Long fileId) {

        if (fileId == null) {
            throw new IllegalArgumentException(
                    "File ID cannot be null"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageFile
                        ));

        if (file.getOwner() == null) {
            throw new ResourceNotFoundException(
                    "File owner not found"
            );
        }

        if (file.getOwner().getId() == null ||
                currentUser.getId() == null) {
            throw new ResourceNotFoundException(
                    "Invalid user information"
            );
        }

        boolean isOwner =
                file.getOwner().getId().equals(currentUser.getId());

        boolean isShared =
                fileShareRepository.existsByFileIdAndSharedWithId(
                        fileId,
                        currentUser.getId()
                );

        if (!isOwner && !isShared) {
            throw new ForbiddenException(
                    "You don't have access to this file"
            );
        }

        if (file.getStoragePath() == null ||
                file.getStoragePath().isBlank()) {
            throw new ResourceNotFoundException(
                    "File storage path not found"
            );
        }

        Path path = Paths.get(file.getStoragePath());

        try {
            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "File cannot be read"
                );
            }

            return resource;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read file",
                    e
            );
        }
    }

    @Override
    public void deleteFile(Long fileId) {

        if (fileId == null) {
            throw new IllegalArgumentException(
                    "File ID cannot be null"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageFile
                        ));

        if (file.getOwner() == null ||
                file.getOwner().getId() == null ||
                owner.getId() == null) {
            throw new ResourceNotFoundException(
                    "File owner not found"
            );
        }

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException(
                    "You don't have permission to delete this file"
            );
        }

        try {
            if (file.getStoragePath() != null &&
                    !file.getStoragePath().isBlank()) {

                Path path = Paths.get(file.getStoragePath());

                Files.deleteIfExists(path);
            }

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

        if (fileId == null) {
            throw new IllegalArgumentException(
                    "File ID cannot be null"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String ownerEmail = authentication.getName();

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Owner not found"
                        ));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageFile
                        ));

        if (file.getOwner() == null ||
                file.getOwner().getId() == null ||
                owner.getId() == null) {
            throw new ResourceNotFoundException(
                    "File owner not found"
            );
        }

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException(
                    "You don't have permission to share this file"
            );
        }

        User sharedWith = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User to share with not found"
                        ));

        if (sharedWith.getId() == null) {
            throw new ResourceNotFoundException(
                    "User ID not found"
            );
        }

        if (fileShareRepository.existsByFileIdAndSharedWithId(
                fileId,
                sharedWith.getId()
        )) {
            throw new IllegalArgumentException(
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

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        return fileShareRepository.findBySharedWith(user)
                .stream()
                .map(FileShare::getFile)
                .toList();
    }

    @Override
    public List<User> getFileShares(Long fileId) {

        if (fileId == null) {
            throw new IllegalArgumentException(
                    "File ID cannot be null"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageFile
                        ));

        if (file.getOwner() == null ||
                file.getOwner().getId() == null ||
                owner.getId() == null) {
            throw new ResourceNotFoundException(
                    "File owner not found"
            );
        }

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException(
                    "You don't have permission to view this file's shares"
            );
        }

        return fileShareRepository.findByFile(file)
                .stream()
                .map(FileShare::getSharedWith)
                .toList();
    }

    @Override
    public void revokeShare(Long fileId, Long userId) {

        if (fileId == null || userId == null) {
            throw new IllegalArgumentException(
                    "File ID and user ID cannot be null"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageUser
                        ));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                messageFile
                        ));

        if (file.getOwner() == null ||
                file.getOwner().getId() == null ||
                owner.getId() == null) {
            throw new ResourceNotFoundException(
                    "File owner not found"
            );
        }

        if (!file.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException(
                    "You don't have permission to modify this file"
            );
        }

        if (!fileShareRepository.existsByFileIdAndSharedWithId(
                fileId,
                userId
        )) {
            throw new ResourceNotFoundException(
                    "File is not shared with this user"
            );
        }

        fileShareRepository.deleteByFileIdAndSharedWithId(
                fileId,
                userId
        );
    }
}