package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.MailBody;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // send
    public void sendEmail(MailBody mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom("test@gmail.com");
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

        javaMailSender.send(message);
    }
}
