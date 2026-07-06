package com.workerconnect.service.notification.templet;

import org.springframework.stereotype.Service;

import com.workerconnect.enums.NotificationType;
import com.workerconnect.service.notification.dto.EmailMessage;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

@Service
public class BookingConfirmationEmailTemplet implements EmailTemplet {

    @Override
    public NotificationType getType() {
        return NotificationType.BOOKING_CONFIRMATION;
    }

    @Override
    public EmailMessage build(NotificationRequestDto request) {
        String userName =request.getData().get("userName");
        String bookingNumber = request.getData().get("bookingNumber");
        String workerName =request.getData().get("workerName");

        String subject = "Booking Confirmation - " + bookingNumber;
        String body ="<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Booking Confirmation</h3>"
                + "<p>Dear " + userName + ",</p>"
                + "<p>Your booking has been confirmed!</p>"
                + "<p><strong>Booking Number:</strong> " + bookingNumber + "</p>"
                + "<p><strong>Worker:</strong> " + workerName + "</p>"
                + "<p>Please visit your dashboard to view details and proceed with the agreement.</p>"
                + "<a href='http://localhost:8080/user/bookings' "
                + "style='background:#2563eb;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>View Booking</a>"
                + "</div>";

        return EmailMessage.builder()
                .to(request.getRecipient())
                .subject(subject)
                .body(body)
                .build();
        
    }

}
