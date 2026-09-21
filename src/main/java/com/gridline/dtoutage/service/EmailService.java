package com.gridline.dtoutage.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender sender;
    @Value("${app.mail.from:no-reply@gridline.local}") private String from;
    @Value("${app.frontend-url:http://localhost:3000}") private String frontendUrl;
    public void sendReset(String email, String name, String token) {
        SimpleMailMessage mail = new SimpleMailMessage(); mail.setFrom(from); mail.setTo(email);
        mail.setSubject("Reset your Gridline password");
        mail.setText("Hello " + name + ",\n\nA password reset was requested for your Gridline account. Use this link within 30 minutes:\n\n" + frontendUrl + "/reset-password?token=" + token + "\n\nIf you did not request this, you can ignore this email. Your password will not change until the link is used.");
        sender.send(mail);
    }
}
