package com.workerconnect.service.notification;

import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

public interface NotificationSender {

    NotificationChannel getNotificationChannel();
    void sendNotification(NotificationRequestDto request);
}
