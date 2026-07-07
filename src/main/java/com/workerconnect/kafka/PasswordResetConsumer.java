package com.workerconnect.kafka;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.workerconnect.dto.PasswordRestDto;
import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.NotificationSender;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetConsumer {

    private final NotificationSender notificationSender;

    

    @KafkaListener(topics = "password-reset")
    public void consumePasswordRest(PasswordRestDto dto){
        System.out.println("**********************************************");
        System.out.println("kafka consumer ");
        System.out.println("Received password reset request for email: " + dto.getEmail() + " with token: " + dto.getToken());
        System.out.println("**********************************************");
         NotificationRequestDto passwordReset=NotificationRequestDto
                                                                .builder()
                                                                .channel(NotificationChannel.EMAIL)
                                                                .type(NotificationType.PASSWORD_RESET)
                                                                .recipient(dto.getEmail())
                                                                .data(Map.of(
                                                                    "token",dto.getToken()
                                                                ))
                                                                .build();
        notificationSender.sendNotification(passwordReset);
    }

}
