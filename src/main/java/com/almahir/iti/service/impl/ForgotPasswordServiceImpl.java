package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.dto.request.MailBody;
import com.almahir.iti.model.ForgotPassword;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.ForgotPasswordRepository;
import com.almahir.iti.service.ForgotPasswordService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordServiceImpl(ForgotPasswordRepository forgotPasswordRepository,
                                     EmailService emailService,
                                     PasswordEncoder passwordEncoder) {
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void generateAndSendOtp(User user) {
        int otp = 100000 + new java.util.Random().nextInt(800000);

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expiryTime(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .user(user)
                .build();
        forgotPasswordRepository.save(forgotPassword);

        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Password Reset OTP")
                .text("This is the OTP for password reset: " + otp)
                .build();
        emailService.sendEmail(mailBody);
    }

    @Override
    @Transactional
    public void verifyOtp(Integer otp, User user) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP for email: " + user.getEmail()));

        if (forgotPassword.getExpiryTime().before(new Date())) {
            forgotPasswordRepository.delete(forgotPassword);
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        forgotPassword.setVerified(true);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        if(!changePasswordRequest.password().equals(changePasswordRequest.confirmPassword())){
            throw new IllegalArgumentException("Passwords do not match");
        }

        ForgotPassword forgotPassword = forgotPasswordRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("No forgot password request found for email: " + user.getEmail()));

        if (!forgotPassword.isVerified()) {
            throw new IllegalArgumentException("OTP not verified. Please verify the OTP first.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.password()));
        forgotPasswordRepository.delete(forgotPassword);
    }
}
