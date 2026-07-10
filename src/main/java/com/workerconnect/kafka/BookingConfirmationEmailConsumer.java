package com.workerconnect.kafka;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.workerconnect.dto.BookingConfirmationKafkaDto;
import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.NotificationSender;
import com.workerconnect.service.notification.dto.NotificationRequestDto;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingConfirmationEmailConsumer {
    private final NotificationSender notifiaction;
    

    @KafkaListener(topics = "booking-confirmation",
                    groupId = "customer-group"
                )
    public void customerEmailConsumer(BookingConfirmationKafkaDto booking){
        NotificationRequestDto notificationRequestUser = NotificationRequestDto.builder()
                .type(NotificationType.BOOKING_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipient(booking.getCustomerEmail()) 
                .data(Map.of(
                        "userName", booking.getCustomerName(),
                        "bookingNumber", booking.getBookingNo(),
                        "workerName",booking.getWorkerName()
                ))
                .build();

        notifiaction.sendNotification(notificationRequestUser);
        
    }


    @KafkaListener(topics = "booking-confirmation",
                    groupId="worker-group"
                )
    public void workerEmailConsumer(BookingConfirmationKafkaDto booking){

        NotificationRequestDto notificationRequestWorker = NotificationRequestDto.builder()
                .type(NotificationType.BOOKING_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipient(booking.getWorkerEmail())
                .data(Map.of(
                        "userName", booking.getCustomerName(),
                        "bookingNumber", booking.getBookingNo(),
                        "workerName",booking.getWorkerName()
                ))
                .build();

        notifiaction.sendNotification(notificationRequestWorker);


    }

}
