package com.almahir.iti.service;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.model.User;

public interface ForgotPasswordService {
    void generateAndSendOtp(String email);
    void verifyOtp(Integer otp, String email);
    void changePassword(User user, ChangePasswordRequest changePasswordRequest);
}
