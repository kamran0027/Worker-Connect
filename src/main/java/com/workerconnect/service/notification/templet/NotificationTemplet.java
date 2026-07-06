package com.workerconnect.service.notification.templet;

import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

public interface NotificationTemplet {
    NotificationType getType();

    String build(NotificationRequestDto request);

}
