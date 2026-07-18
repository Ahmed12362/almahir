package com.almahir.iti.service;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.model.User;

public interface ForgotPasswordService {
    void generateAndSendOtp(User user);
    void verifyOtp(Integer otp, User user);
    void changePassword(User user, ChangePasswordRequest changePasswordRequest);
}
