
package com.souha.securefilesharingplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String recipientEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Verify your email - Secure File Sharing");
        message.setText(
                "Hello,\n\n" +
                "Your email verification code is: " + otp + "\n\n" +
                "This code expires in 5 minutes. " +
                "If you did not request it, you can ignore this email."
        );

        mailSender.send(message);
    }
}
