package com.workerconnect.service;

import com.workerconnect.dto.BookingDto;
import com.workerconnect.enums.AgreementStatus;
import com.workerconnect.enums.BookingStatus;
import com.workerconnect.enums.NotificationChannel;
import com.workerconnect.enums.NotificationType;
import com.workerconnect.model.*;
import com.workerconnect.repository.*;
import com.workerconnect.service.notification.NotificationSender;
import com.workerconnect.service.notification.dto.NotificationRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final AgreementRepository agreementRepository;
    private final EmailService emailService;
    private final NotificationSender notificationSender;

    @Transactional
    public Booking createBooking(Long userId, BookingDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Worker worker = workerRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        String bookingNumber = "WC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .user(user)
                .worker(worker)
                .bookingDate(dto.getBookingDate())
                .bookingTime(dto.getBookingTime())
                .startDate(dto.getStartDate())
                .completionDate(dto.getCompletionDate())
                .workDescription(dto.getWorkDescription())
                .serviceType(dto.getServiceType())
                .amount(dto.getAmount())
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        // Auto-create agreement
        Agreement agreement = Agreement.builder()
                .booking(booking)
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .userPhone(user.getPhone())
                .userAddress(user.getAddress())
                .workerName(worker.getFullName())
                .workerEmail(worker.getEmail())
                .workerPhone(worker.getPhone())
                .workerProfession(worker.getProfession())
                .serviceDescription(dto.getWorkDescription())
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .completionDate(dto.getCompletionDate())
                .status(AgreementStatus.PENDING)
                .build();
        agreementRepository.save(agreement);
        
        // emailService.sendBookingConfirmation(user.getEmail(), user.getFullName(), bookingNumber, worker.getFullName());
        // emailService.sendBookingRequestToWorker(worker.getEmail(), worker.getFullName(), bookingNumber, user.getFullName());
        NotificationRequestDto notificationRequestUser = NotificationRequestDto.builder()
                .type(NotificationType.BOOKING_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipient(user.getEmail()) 
                .data(Map.of(
                        "userName", user.getFullName(),
                        "bookingNumber", bookingNumber,
                        "workerName", worker.getFullName()
                ))
                .build();
        NotificationRequestDto notificationRequestWorker = NotificationRequestDto.builder()
                .type(NotificationType.BOOKING_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipient(worker.getEmail()) 
                .data(Map.of(
                        "workerName", worker.getFullName(),
                        "bookingNumber", bookingNumber,
                        "userName", user.getFullName()
                ))
                .build();
        notificationSender.sendNotification(notificationRequestUser);
        notificationSender.sendNotification(notificationRequestWorker);

        log.info("Booking created with booking number: {} for user: {} and worker: {}", bookingNumber, user.getFullName(), worker.getFullName());
        

        return booking;
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking findByBookingNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getBookingsByWorker(Long workerId) {
        return bookingRepository.findByWorkerIdOrderByCreatedAtDesc(workerId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking acceptBooking(Long bookingId) {
        Booking b = findById(bookingId);
        b.setStatus(BookingStatus.ACCEPTED);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking rejectBooking(Long bookingId, String reason) {
        Booking b = findById(bookingId);
        b.setStatus(BookingStatus.REJECTED);
        b.setCancellationReason(reason);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String reason) {
        Booking b = findById(bookingId);
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancellationReason(reason);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking markWorkStarted(Long bookingId) {
        Booking b = findById(bookingId);
        b.setStatus(BookingStatus.WORK_STARTED);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking markWorkCompleted(Long bookingId) {
        Booking b = findById(bookingId);
        b.setStatus(BookingStatus.COMPLETED);
        Worker worker = b.getWorker();
        Integer currentJobs = worker.getTotalJobsCompleted();
        worker.setTotalJobsCompleted((currentJobs != null ? currentJobs : 0) + 1);
        workerRepository.save(worker);
        return bookingRepository.save(b);
    }

    public boolean hasUserBookedWorker(Long userId, Long workerId) {
        return !bookingRepository.findCompletedByUserAndWorker(userId, workerId).isEmpty();
    }
}
