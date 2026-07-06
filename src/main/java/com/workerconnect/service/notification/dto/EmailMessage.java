package com.workerconnect.service.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {

    private String to;
    private String subject;
    private String body;

}
