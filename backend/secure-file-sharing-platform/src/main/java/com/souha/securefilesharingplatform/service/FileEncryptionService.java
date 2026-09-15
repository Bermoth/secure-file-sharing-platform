package com.souha.securefilesharingplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.core.io.ByteArrayResource;

@Service
public class FileEncryptionService {

    private static final int AES_KEY_SIZE = 32;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public FileEncryptionService(
            @Value("${file.encryption-key}") String encryptionKey
    ) {
        try {
            byte[] keyBytes =
                    Base64.getDecoder().decode(encryptionKey);

            if (keyBytes.length != AES_KEY_SIZE) {
                throw new IllegalArgumentException(
                        "File encryption key must be 32 bytes"
                );
            }

            this.secretKey =
                    new SecretKeySpec(keyBytes, "AES");

            this.secureRandom = new SecureRandom();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid file encryption key. " +
                    "It must be a Base64-encoded 32-byte key.",
                    e
            );
        }
    }

    public void encryptFile(
            InputStream inputStream,
            Path outputPath
    ) throws IOException {

        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(
                    "AES/GCM/NoPadding"
            );

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            try (OutputStream fileOutput =
                         Files.newOutputStream(outputPath)) {

                // Store the IV at the beginning of the file.
                // The IV itself is not secret.
                fileOutput.write(iv);

                try (CipherOutputStream cipherOutput =
                             new CipherOutputStream(
                                     fileOutput,
                                     cipher
                             )) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead =
                            inputStream.read(buffer)) != -1) {

                        cipherOutput.write(
                                buffer,
                                0,
                                bytesRead
                        );
                    }
                }
            }

        } catch (GeneralSecurityException e) {
            throw new IOException(
                    "Could not encrypt file",
                    e
            );
        } finally {
            inputStream.close();
        }
    }

public Resource decryptFile(
        Path encryptedFile,
        String originalFilename
) throws IOException {

    try {
        byte[] encryptedData =
                Files.readAllBytes(encryptedFile);

        if (encryptedData.length <= GCM_IV_LENGTH) {
            throw new IOException(
                    "Encrypted file is invalid"
            );
        }

        byte[] iv =
                new byte[GCM_IV_LENGTH];

        System.arraycopy(
                encryptedData,
                0,
                iv,
                0,
                GCM_IV_LENGTH
        );

        byte[] ciphertext =
                new byte[
                        encryptedData.length -
                        GCM_IV_LENGTH
                ];

        System.arraycopy(
                encryptedData,
                GCM_IV_LENGTH,
                ciphertext,
                0,
                ciphertext.length
        );

        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(
                        GCM_TAG_LENGTH,
                        iv
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                gcmSpec
        );

        byte[] plaintext =
                cipher.doFinal(ciphertext);

        return new ByteArrayResource(
                plaintext
        ) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };

    } catch (GeneralSecurityException e) {
        throw new IOException(
                "Could not decrypt file",
                e
        );
    }
}
}