package com.workerconnect.repository;

import com.workerconnect.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByWorkerIdAndActiveTrue(Long workerId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findAllByActiveTrue();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.worker.id = :workerId AND r.active = true")
    Double getAverageRatingByWorker(Long workerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.worker.id = :workerId AND r.active = true")
    Long countActiveReviewsByWorker(Long workerId);
}
