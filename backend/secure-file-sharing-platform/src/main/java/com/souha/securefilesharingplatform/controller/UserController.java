package com.souha.securefilesharingplatform.controller;

import com.souha.securefilesharingplatform.dto.RegisterRequest;
import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public User createUser(@RequestBody RegisterRequest request) {
        return userService.createUser(request);
    }
}
