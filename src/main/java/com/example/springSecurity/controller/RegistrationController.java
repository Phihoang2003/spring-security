package com.example.springSecurity.controller;

import com.example.springSecurity.entity.User;
import com.example.springSecurity.entity.VerificationToken;
import com.example.springSecurity.event.RegistrationCompleteEvent;
import com.example.springSecurity.model.PasswordModel;
import com.example.springSecurity.model.UserModel;
import com.example.springSecurity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;
    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        User user=userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return "Success";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){
        String result=userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")){
            return "User verifies SuccessFully";
        }
        else{
            return "Bad user";
        }
    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,HttpServletRequest request){
        VerificationToken verificationToken=userService.generateNewVerificationTokenn(oldToken);
        User user=verificationToken.getUser();
        resendVerificationTokenMail(user,applicationUrl(request),verificationToken);
        return "Verification Link Sent";
    }
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel,HttpServletRequest request){

        String url="";
        User user=userService.findUserByEmail(passwordModel.getEmail());

        if(user!=null){
            String token= UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url=passwordResetTokenMail(user,applicationUrl(request),token);

        }
        return url;
    }
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,@RequestBody PasswordModel passwordModel){
        String result=userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "Invalid";
        }
        Optional<User> user=userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(),passwordModel.getNewPassword());
            return "Password Reset SuccessFully";

        }
        else{
            return "Invalid Token";
        }

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url=applicationUrl+"/savePassword?token="+token;
        //sendVerificationEmail
        log.info("Click the link to reset your password:{}",url);
        return url;
    }

    public void resendVerificationTokenMail(User user,String applicationUrl,VerificationToken verificationToken){
        String url=applicationUrl+"/verifyRegistration?token="+verificationToken.getToken();
        //sendVerificationEmail
        log.info("Click the link to verify your account:{}",url);
    }
    private String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
    }
}
