package com.workpilot.service;

import com.workpilot.entity.auth.Role;
import com.workpilot.entity.auth.User;
import com.workpilot.repository.ressources.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public void sendToAdmins(String subject, String content) {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            send(admin.getEmail(), subject, content);
        }
    }

    public void send(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }
}

