package com.workerconnect.service;

import com.workerconnect.model.Booking;
import com.workerconnect.model.Review;
import com.workerconnect.model.User;
import com.workerconnect.model.Worker;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.ReviewRepository;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Review addReview(Long userId, Long workerId, Long bookingId, Integer rating, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new RuntimeException("You have already reviewed this booking");
        }

        Review review = Review.builder()
                .user(user).worker(worker).booking(booking)
                .rating(rating).comment(comment).active(true)
                .build();
        review = reviewRepository.save(review);

        // Update worker rating
        Double avg = reviewRepository.getAverageRatingByWorker(workerId);
        Long count = reviewRepository.countActiveReviewsByWorker(workerId);
        worker.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        worker.setTotalReviews(count.intValue());
        workerRepository.save(worker);

        return review;
    }

    public List<Review> getWorkerReviews(Long workerId) {
        return reviewRepository.findByWorkerIdAndActiveTrue(workerId);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public void deactivateReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        r.setActive(false);
        reviewRepository.save(r);

        // Recalculate worker rating
        Long workerId = r.getWorker().getId();
        Double avg = reviewRepository.getAverageRatingByWorker(workerId);
        Long count = reviewRepository.countActiveReviewsByWorker(workerId);
        Worker worker = r.getWorker();
        worker.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        worker.setTotalReviews(count.intValue());
        workerRepository.save(worker);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public boolean hasReviewedBooking(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId).isPresent();
    }
}
