package com.photoconnect.service;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;

public interface AuthService {
    User authenticate(LoginRequest loginRequest);
}
