package com.photoconnect.service;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.entity.User;

public interface UserService {
    User registerUser(RegisterRequest request);
}
