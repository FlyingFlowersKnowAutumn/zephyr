package com.example.blog.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("【微风日志】验证码");
            message.setText(String.format(
                    "您好，您正在进行微风日志的验证操作。\n\n" +
                    "您的验证码是：%s\n\n" +
                    "验证码有效期为5分钟，请及时使用。\n\n" +
                    "如果不是您本人操作，请忽略此邮件。", code));

            mailSender.send(message);
            log.info("Verification code sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", toEmail, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}