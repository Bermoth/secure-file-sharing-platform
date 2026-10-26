
package com.souha.securefilesharingplatform.repository;

import com.souha.securefilesharingplatform.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    void deleteByEmail(String email);
}

