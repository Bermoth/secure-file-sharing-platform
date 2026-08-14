package com.souha.securefilesharingplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.souha.securefilesharingplatform.entity.File;
import com.souha.securefilesharingplatform.entity.User;

public interface FileRepository extends JpaRepository<File,Long>  {
    List<File> findByOwner(User owner);
}
