package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender,
                            ApplicationProperties applicationProperties) {
        this.mailSender = mailSender;
        this.applicationProperties = applicationProperties;
    }

    @Async
    @Override
    public void sendMimeEmail(String to, String subject, String mimeText, boolean html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setText(mimeText, html);
            helper.setSubject(subject);
            helper.setFrom(applicationProperties.getMailFrom());
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Email MessagingException on sendMimeEmail: {}", e.getMessage());
        }
    }

}
