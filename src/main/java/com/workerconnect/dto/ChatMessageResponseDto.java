package com.workerconnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto{

    private Long id;
    private Long senderId;
    private String senderName;
    private String message;
    private String sentTime;

}
