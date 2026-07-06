package com.workerconnect.service.notification.templet;

import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.dto.EmailMessage;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

public interface EmailTemplet {

    NotificationType getType();

    EmailMessage build(NotificationRequestDto request);
}
