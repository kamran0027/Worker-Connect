package com.workerconnect.service.notification.dto;

import java.util.Map;

import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {
    private NotificationType type;

    private NotificationChannel channel;

    private String recipient;

    private Map<String,String> data;

}
