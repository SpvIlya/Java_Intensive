package app.service;

import app.dto.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "${kafka.topic.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(UserEvent event) {
        logger.info("Получено событие из Kafka: email={}, operation={}", event.getEmail(), event.getOperation());

        try {
            if ("CREATED".equals(event.getOperation())) {
                emailService.sendAccountCreatedEmail(event.getEmail());
            } else if ("DELETED".equals(event.getOperation())) {
                emailService.sendAccountDeletedEmail(event.getEmail());
            } else {
                logger.warn("Неизвестная операция: {}", event.getOperation());
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке письма: {}", e.getMessage());
        }
    }
}
