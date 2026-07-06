package com.workerconnect.service.notification;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.dto.EmailMessage;
import com.workerconnect.service.notification.dto.NotificationRequestDto;
import com.workerconnect.service.notification.templet.EmailTemplet;

import jakarta.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class EmailNotificationSender implements NotificationSender{

    private final JavaMailSender mailSender;

    private final Map<NotificationType, EmailTemplet> templets;

    @Value("${spring.mail.username}")
    private String fromEmail;
    

    public EmailNotificationSender(JavaMailSender mailSender,List<EmailTemplet> emailTemplets) {
        this.mailSender = mailSender;
        this.templets = emailTemplets.stream()
                .collect(Collectors.toMap(EmailTemplet::getType, Function.identity()));
    }



    @Override
    public NotificationChannel getNotificationChannel(){
        return NotificationChannel.EMAIL;
    }

    @Override
    @Async
    public void sendNotification(NotificationRequestDto request){
        
        try {
            EmailMessage email=templets.get(request.getType()).build(request);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true);
            mailSender.send(message);

        } catch (Exception e) {
            // TODO: handle exception
            log.error("Failed to send email notification: {}", e.getMessage(), e);
        }
    }

}
