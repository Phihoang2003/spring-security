package com.example.springSecurity.service;

import com.example.springSecurity.entity.User;
import com.example.springSecurity.entity.VerificationToken;
import com.example.springSecurity.model.UserModel;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationTokenn(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);
}
