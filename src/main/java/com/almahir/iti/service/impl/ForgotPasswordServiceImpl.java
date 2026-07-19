package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.ChangePasswordRequest;
import com.almahir.iti.dto.request.MailBody;
import com.almahir.iti.model.ForgotPassword;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.ForgotPasswordRepository;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.ForgotPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Random;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public ForgotPasswordServiceImpl(ForgotPasswordRepository forgotPasswordRepository,
                                     EmailService emailService,
                                     PasswordEncoder passwordEncoder,
                                     UserRepository userRepository) {
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void generateAndSendOtp(String email) {
        int otp = 100000 + new Random().nextInt(800000);

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .email(email)
                .otp(otp)
                .timeToLive(300)
                .build();
        forgotPasswordRepository.save(forgotPassword);

        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("Password Reset OTP")
                .text("This is the OTP for password reset: " + otp)
                .build();
        emailService.sendEmail(mailBody);
    }

    @Override
    public void verifyOtp(Integer otp, String email) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP for email: " + email));

        if (!forgotPassword.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP for email: " + email);
        }

        forgotPassword.setVerified(true);
        forgotPasswordRepository.save(forgotPassword);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        if (!changePasswordRequest.password().equals(changePasswordRequest.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        ForgotPassword forgotPassword = forgotPasswordRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No forgot password request found or session expired for email: " + user.getEmail()));

        if (!forgotPassword.isVerified()) {
            throw new IllegalArgumentException("OTP not verified. Please verify the OTP first.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.password()));
        userRepository.save(user);

        forgotPasswordRepository.delete(forgotPassword);
    }
}