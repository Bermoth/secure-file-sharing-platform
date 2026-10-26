
package com.souha.securefilesharingplatform.service;

import com.souha.securefilesharingplatform.entity.EmailVerification;
import com.souha.securefilesharingplatform.repository.EmailVerificationRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationRepository repository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String otpSecret;

    public OtpService(
            EmailVerificationRepository repository,
            EmailService emailService,
            @Value("${OTP_SECRET}") String otpSecret
    ) {
        this.repository = repository;
        this.emailService = emailService;
        this.otpSecret = otpSecret;
    }

    @Transactional
    public void sendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        String otpHash = hashOtp(normalizedEmail, otp);

        EmailVerification verification = repository
                .findByEmail(normalizedEmail)
                .orElseGet(EmailVerification::new);

        verification.setEmail(normalizedEmail);
        verification.setOtpHash(otpHash);
        verification.setExpiresAt(
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        );
        verification.setAttempts(0);

        repository.save(verification);

        // Send only after the database transaction commits.
        emailService.sendOtp(normalizedEmail, otp);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public boolean verifyOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();

        EmailVerification verification = repository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or expired verification code"
                        )
                );

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())
                || verification.getAttempts() >= MAX_ATTEMPTS) {
            repository.delete(verification);
            throw new IllegalArgumentException(
                    "Invalid or expired verification code"
            );
        }

        verification.setAttempts(verification.getAttempts() + 1);

        String submittedHash = hashOtp(normalizedEmail, otp);

        boolean matches = MessageDigest.isEqual(
                submittedHash.getBytes(StandardCharsets.UTF_8),
                verification.getOtpHash().getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {
            if (verification.getAttempts() >= MAX_ATTEMPTS) {
                repository.delete(verification);
            } else {
                repository.save(verification);
            }

            throw new IllegalArgumentException(
                    "Invalid or expired verification code"
            );
        }

        repository.delete(verification);
        return true;
    }

    private String hashOtp(String email, String otp) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] input = (otpSecret + ":" + email + ":" + otp)
                    .getBytes(StandardCharsets.UTF_8);

            return HexFormat.of().formatHex(digest.digest(input));

        } catch (Exception e) {
            throw new IllegalStateException("Could not hash OTP", e);
        }
    }
}
