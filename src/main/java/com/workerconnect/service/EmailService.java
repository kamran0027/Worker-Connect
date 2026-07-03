package com.workerconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@workerconnect.com}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "WorkerConnect - Password Reset Request";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Password Reset Request</h3>"
                + "<p>Click the button below to reset your password. This link expires in 1 hour.</p>"
                + "<a href='http://localhost:8080/auth/reset-password?token=" + token + "' "
                + "style='background:#2563eb;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>Reset Password</a>"
                + "<p style='margin-top:20px;color:#6b7280;'>If you did not request this, please ignore this email.</p>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendBookingConfirmation(String toEmail, String userName, String bookingNumber, String workerName) {
        String subject = "WorkerConnect - Booking Confirmed #" + bookingNumber;
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
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
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendBookingRequestToWorker(String toEmail, String workerName, String bookingNumber, String userName) {
        String subject = "WorkerConnect - New Booking Request #" + bookingNumber;
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>New Booking Request</h3>"
                + "<p>Dear " + workerName + ",</p>"
                + "<p>You have received a new booking request from <strong>" + userName + "</strong>.</p>"
                + "<p><strong>Booking Number:</strong> " + bookingNumber + "</p>"
                + "<p>Please login to your dashboard to accept or reject the request.</p>"
                + "<a href='http://localhost:8080/worker/bookings' "
                + "style='background:#16a34a;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>View Request</a>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendPaymentConfirmation(String toEmail, String userName, String bookingNumber, String amount) {
        String subject = "WorkerConnect - Payment Successful #" + bookingNumber;
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Payment Successful</h3>"
                + "<p>Dear " + userName + ",</p>"
                + "<p>Your payment of <strong>₹" + amount + "</strong> has been received.</p>"
                + "<p><strong>Booking Number:</strong> " + bookingNumber + "</p>"
                + "<a href='http://localhost:8080/user/payments' "
                + "style='background:#2563eb;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>View Receipt</a>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendWorkerApprovalEmail(String toEmail, String workerName) {
        String subject = "WorkerConnect - Your Profile Has Been Approved!";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Profile Approved 🎉</h3>"
                + "<p>Dear " + workerName + ",</p>"
                + "<p>Congratulations! Your worker profile has been approved. You can now receive bookings.</p>"
                + "<a href='http://localhost:8080/auth/login' "
                + "style='background:#16a34a;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;'>Go to Dashboard</a>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendWorkerRejectionEmail(String toEmail, String workerName, String reason) {
        String subject = "WorkerConnect - Profile Verification Update";
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2563eb;'>WorkerConnect</h2>"
                + "<h3>Profile Verification Update</h3>"
                + "<p>Dear " + workerName + ",</p>"
                + "<p>Unfortunately, your profile could not be approved at this time.</p>"
                + "<p><strong>Reason:</strong> " + reason + "</p>"
                + "<p>Please contact support for more information.</p>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
