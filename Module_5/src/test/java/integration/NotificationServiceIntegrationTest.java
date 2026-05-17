package integration;

import app.NotificationServiceApplication;
import app.dto.SendEmailRequest;
import app.dto.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = NotificationServiceApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@DisplayName("Интеграционные тесты Notification Service")
class NotificationServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetup.SMTP);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> greenMail.getSmtp().getBindTo());
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
        registry.add("mail.from", () -> "noreply@test.com");
    }

    @BeforeEach
    void setUp() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    @DisplayName("Отправка письма через API при создании аккаунта")
    void testSendCreatedEmailViaApi() throws Exception {
        mockMvc.perform(post("/api/notifications/send-created")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Письмо о создании аккаунта отправлено на user@example.com"));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        String body = (String) messages[0].getContent();
        assertThat(body).contains("аккаунт на сайте был успешно создан");
        assertThat(messages[0].getAllRecipients()[0].toString()).contains("user@example.com");
    }

    @Test
    @DisplayName("Отправка письма через API при удалении аккаунта")
    void testSendDeletedEmailViaApi() throws Exception {
        mockMvc.perform(post("/api/notifications/send-deleted")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Письмо об удалении аккаунта отправлено на user@example.com"));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        String body = (String) messages[0].getContent();
        assertThat(body).contains("аккаунт был удалён");
    }

    @Test
    @DisplayName("Отправка кастомного письма через API POST /send")
    void testSendCustomEmailViaApi() throws Exception {
        SendEmailRequest request = new SendEmailRequest();
        request.setEmail("user@example.com");
        request.setSubject("Тестовая тема");
        request.setBody("Тестовое тело письма");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Письмо успешно отправлено на user@example.com"));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getSubject()).isEqualTo("Тестовая тема");
    }

    @Test
    @DisplayName("Получение события из Kafka и отправка письма (создание)")
    void testConsumeUserCreatedEventFromKafka() throws Exception {
        UserEvent event = new UserEvent("user@example.com", "CREATED");

        kafkaTemplate.send("user-events", event.getEmail(), event);
        Thread.sleep(2000);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        String body = (String) messages[0].getContent();
        assertThat(body).contains("аккаунт на сайте был успешно создан");
    }

    @Test
    @DisplayName("Получение события из Kafka и отправка письма (удаление)")
    void testConsumeUserDeletedEventFromKafka() throws Exception {
        UserEvent event = new UserEvent("user@example.com", "DELETED");

        kafkaTemplate.send("user-events", event.getEmail(), event);
        Thread.sleep(2000);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        String body = (String) messages[0].getContent();
        assertThat(body).contains("аккаунт был удалён");
    }

    @Test
    @DisplayName("Валидация ошибки при пустом email в кастомном запросе")
    void testValidationErrorWhenEmailEmpty() throws Exception {
        SendEmailRequest request = new SendEmailRequest();
        request.setEmail("");
        request.setSubject("Тема");
        request.setBody("Тело");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }
}
