package com.workerconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BookingConfirmationKafkaDto {

    private String bookingNo;
    private String customerEmail;
    private String customerName;
    private String workerEmail;
    private String workerName;

}
