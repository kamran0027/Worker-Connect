package com.workerconnect.repository;

import com.workerconnect.enums.BookingStatus;
import com.workerconnect.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingNumber(String bookingNumber);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByWorkerId(Long workerId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByWorkerIdOrderByCreatedAtDesc(Long workerId);

    long countByStatus(BookingStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.worker.id = :workerId AND b.status = 'COMPLETED'")
    List<Booking> findCompletedByUserAndWorker(Long userId, Long workerId);
}
