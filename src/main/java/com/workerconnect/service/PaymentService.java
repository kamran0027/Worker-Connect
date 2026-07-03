package com.workerconnect.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import com.workerconnect.enums.PaymentStatus;
import com.workerconnect.model.Booking;
import com.workerconnect.model.Payment;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.PaymentRepository;
import com.workerconnect.util.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final com.workerconnect.repository.WorkerRepository workerRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;


    // ─── Razorpay ────────────────────────────────────────────────────────────

    @Transactional
    public Payment createRazorpayOrder(Long bookingId) throws RazorpayException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", booking.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "WC-" + bookingId);

        Order razorpayOrder = client.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .booking(booking)
                .orderId("WC-" + bookingId + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .paymentMethod("RAZORPAY")
                .amount(booking.getAmount())
                .currency("INR")
                .razorpayOrderId(razorpayOrder.get("id"))
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment verifyRazorpayPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        log.info("Found payment for razorpayOrderId={}: id={}, currentStatus={}", razorpayOrderId, payment.getId(), payment.getStatus());
        // Signature verification (simplified - use HMAC SHA256 in prod)
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setTransactionId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        payment = paymentRepository.save(payment);
        log.info("Payment updated to COMPLETED: id={}", payment.getId());
        postPaymentActions(payment);
        return payment;
    }

  // cash on delivery
    @Transactional
    public Payment createCODPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Payment payment = Payment.builder()
                .booking(booking)
                .orderId("WC-" + bookingId + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .paymentMethod("CASH")
                .amount(booking.getAmount())
                .currency("INR")
                .status(PaymentStatus.COMPLETED)
                .build();
        paymentRepository.save(payment);
        postPaymentActions(payment);
        return payment;
    }

    // ─── Common ──────────────────────────────────────────────────────────────

    private void postPaymentActions(Payment payment) {
        Booking booking = payment.getBooking();
        // Update worker earnings (guard against null)
        try {
            if (booking.getWorker() != null) {
                java.math.BigDecimal current = booking.getWorker().getTotalEarnings();
                if (current == null) current = java.math.BigDecimal.ZERO;
                booking.getWorker().setTotalEarnings(current.add(payment.getAmount()));
                // persist worker
                try { workerRepository.save(booking.getWorker()); } catch (Exception e) { log.error("Failed to save worker earnings: {}", e.getMessage()); }
            }
        } catch (Exception e) {
            log.error("Failed updating worker earnings: {}", e.getMessage());
        }

        // Send confirmation email (non-fatal)
        try {
            emailService.sendPaymentConfirmation(
                    booking.getUser().getEmail(),
                    booking.getUser().getFullName(),
                    booking.getBookingNumber(),
                    payment.getAmount().toString());
        } catch (Exception e) {
            log.error("Failed to send payment email: {}", e.getMessage());
        }

        // Generate invoice PDF
        try {
            byte[] invoicePdf = pdfGeneratorService.generateInvoice(booking, payment);
            String path = fileStorageService.storeInvoice(invoicePdf, "INV-" + booking.getBookingNumber() + ".pdf");
            payment.setInvoiceFilePath(path);
            paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Failed to generate invoice: {}", e.getMessage());
        }
    }

    public Payment findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).orElse(null);
    }

    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByBookingUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Payment> getPaymentsByWorker(Long workerId) {
        return paymentRepository.findByBookingWorkerIdOrderByCreatedAtDesc(workerId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal rev = paymentRepository.getTotalRevenue();
        return rev != null ? rev : BigDecimal.ZERO;
    }

    public BigDecimal getWorkerEarnings(Long workerId) {
        BigDecimal earn = paymentRepository.getTotalEarningsByWorker(workerId);
        return earn != null ? earn : BigDecimal.ZERO;
    }

    public byte[] generateInvoicePdf(Long bookingId) throws Exception {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return pdfGeneratorService.generateInvoice(payment.getBooking(), payment);
    }

    public String getRazorpayKeyId() { return razorpayKeyId; }

    
    
}
