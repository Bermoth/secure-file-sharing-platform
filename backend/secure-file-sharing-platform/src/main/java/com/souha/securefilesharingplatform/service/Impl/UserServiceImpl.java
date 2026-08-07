package com.souha.securefilesharingplatform.service.impl;

import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.repository.UserRepository;
import com.souha.securefilesharingplatform.service.UserService;
import com.souha.securefilesharingplatform.dto.RegisterRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
}


@Override
public User createUser(RegisterRequest request) {

    if(userRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }

    User user = new User();

    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole("USER");

    return userRepository.save(user);
    }
}