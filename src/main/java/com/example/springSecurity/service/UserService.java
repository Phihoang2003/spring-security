package com.example.springSecurity.service;

import com.example.springSecurity.entity.User;
import com.example.springSecurity.model.UserModel;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);
}
