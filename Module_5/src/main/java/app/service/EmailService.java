package app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAccountCreatedEmail(String toEmail) {
        String subject = "Добро пожаловать!";
        String body = "Здравствуйте! Ваш аккаунт на сайте был успешно создан.";
        sendEmail(toEmail, subject, body);
        logger.info("Отправлено письмо о создании аккаунта на {}", toEmail);
    }

    public void sendAccountDeletedEmail(String toEmail) {
        String subject = "Удаление аккаунта";
        String body = "Здравствуйте! Ваш аккаунт был удалён.";
        sendEmail(toEmail, subject, body);
        logger.info("Отправлено письмо об удалении аккаунта на {}", toEmail);
    }

    public void sendCustomEmail(String toEmail, String subject, String body) {
        sendEmail(toEmail, subject, body);
        logger.info("Отправлено кастомное письмо на {}", toEmail);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}