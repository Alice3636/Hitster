package com.hitster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);
    private static final String FROM_ADDRESS = "hitster@test-eqvygm0y06jl0p7w.mlsender.net";

    private static EmailSenderService instance;

    private final JavaMailSender mailSender;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void init() {
        EmailSenderService.instance = this;
    }

    public static void sendEmail(String to, String subject, String text) {
        if (instance == null) {
            log.error("EmailSenderService is not initialized yet!");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {

            instance.mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}