package app.kafka;

import app.dto.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);

    @Value("${kafka.topic.user-events}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {
        logger.info("Отправка события в Kafka: email={}, operation={}", event.getEmail(), event.getOperation());
        kafkaTemplate.send(topic, event.getEmail(), event);
    }
}
