package com.workerconnect.service.notification.templet;

import org.springframework.stereotype.Service;

import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.dto.EmailMessage;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

@Service
public class SendPasswordResetEmail implements EmailTemplet {

    @Override
    public NotificationType getType() {
        return NotificationType.PASSWORD_RESET;
    }

    @Override
    public EmailMessage build(NotificationRequestDto request) {
        String token=request.getData().get("token");
        String subject = "Password Reset Request";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Password Reset Request</h3>"
                + "<p>Click the button below to reset your password. This link expires in 1 hour.</p>"
                + "<a href='http://localhost:8080/auth/reset-password?token=" + token + "' "
                + "style='background:#2563eb;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>Reset Password</a>"
                + "<p style='margin-top:20px;color:#6b7280;'>If you did not request this, please ignore this email.</p>"
                + "</div>";

        return EmailMessage.builder()
                            .to(request.getRecipient())
                            .subject(subject)
                            .body(body)
                            .build();
    }

}
