package com.souha.securefilesharingplatform.service;

import com.souha.securefilesharingplatform.entity.User;
import com.souha.securefilesharingplatform.dto.RegisterRequest;

public interface UserService {
    User createUser(RegisterRequest request);
}
