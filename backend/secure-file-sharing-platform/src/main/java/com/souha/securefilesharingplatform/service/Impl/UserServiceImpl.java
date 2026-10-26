
package com.souha.securefilesharingplatform.service.impl;

import com.souha.securefilesharingplatform.dto.RegisterRequest;
import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.repository.UserRepository;
import com.souha.securefilesharingplatform.service.OtpService;
import com.souha.securefilesharingplatform.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override
    @Transactional
    public User createUser(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        otpService.sendOtp(email);

        return savedUser;
    }
}
