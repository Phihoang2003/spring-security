package com.example.springSecurity.service;

import com.example.springSecurity.entity.PasswordResetToken;
import com.example.springSecurity.entity.User;
import com.example.springSecurity.entity.VerificationToken;
import com.example.springSecurity.model.UserModel;
import com.example.springSecurity.repository.PasswordResetTokenRepository;
import com.example.springSecurity.repository.UserRepository;
import com.example.springSecurity.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public User registerUser(UserModel userModel) {
        User user=new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));

        userRepository.save(user);
        return user;

    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken=
                new VerificationToken(user,token);
        verificationTokenRepository.save(verificationToken);


    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken=
                verificationTokenRepository.findByToken(token);
        if(verificationToken==null){
            return "invalid";
        }
        User user=verificationToken.getUser();
        Calendar calendar=Calendar.getInstance();
        if(verificationToken.getExpirationTime().getTime()-calendar.getTime().getTime()<=0){
            return "expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";

    }

    @Override
    public VerificationToken generateNewVerificationTokenn(String oldToken) {
        VerificationToken verificationToken=verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken=new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((passwordResetToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";

    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
