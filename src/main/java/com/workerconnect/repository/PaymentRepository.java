package com.workerconnect.repository;

import com.workerconnect.enums.PaymentStatus;
import com.workerconnect.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByRazorpayOrderId(String orderId);
    //Optional<Payment> findByStripeSessionId(String sessionId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByBookingUserIdOrderByCreatedAtDesc(Long userId);
    List<Payment> findByBookingWorkerIdOrderByCreatedAtDesc(Long workerId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.booking.worker.id = :workerId")
    BigDecimal getTotalEarningsByWorker(Long workerId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
}
