package com.souha.securefilesharingplatform.controller;

import com.souha.securefilesharingplatform.dto.LoginRequest;
import com.souha.securefilesharingplatform.dto.VerifyOtpRequest;
import com.souha.securefilesharingplatform.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.repository.UserRepository;
import com.souha.securefilesharingplatform.service.OtpService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserRepository userRepository,
        OtpService otpService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.otpService = otpService;
}

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
        return jwtService.generateToken((UserDetails) authentication.getPrincipal());
    }

    @PostMapping("/verify-otp")
@Transactional
public String verifyOtp(@RequestBody VerifyOtpRequest request) {
    boolean valid = otpService.verifyOtp(
            request.getEmail(),
            request.getOtp()
    );

    if (!valid) {
        throw new IllegalArgumentException("Invalid verification code");
    }

    User user = userRepository.findByEmail(
            request.getEmail().trim().toLowerCase()
    ).orElseThrow(() ->
            new IllegalArgumentException("User not found"));

    user.setEmailVerified(true);
    userRepository.save(user);

    return "Email verified successfully. You can now log in.";
}
}