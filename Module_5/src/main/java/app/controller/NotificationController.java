package app.controller;

import app.dto.SendEmailRequest;
import app.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailService.sendCustomEmail(request.getEmail(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("Письмо успешно отправлено на " + request.getEmail());
    }

    @PostMapping("/send-created")
    public ResponseEntity<String> sendCreatedEmail(@RequestParam String email) {
        emailService.sendAccountCreatedEmail(email);
        return ResponseEntity.ok("Письмо о создании аккаунта отправлено на " + email);
    }

    @PostMapping("/send-deleted")
    public ResponseEntity<String> sendDeletedEmail(@RequestParam String email) {
        emailService.sendAccountDeletedEmail(email);
        return ResponseEntity.ok("Письмо об удалении аккаунта отправлено на " + email);
    }
}
