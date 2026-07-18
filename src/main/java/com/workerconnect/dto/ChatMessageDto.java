package com.workerconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private Long bookingId;
    private Long senderId;
    private Long receiverId;
    private String message;

}
