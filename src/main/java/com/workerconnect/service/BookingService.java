package com.workerconnect.service;

import com.workerconnect.dto.AgreementDto;
import com.workerconnect.dto.BookingConfirmationKafkaDto;
import com.workerconnect.dto.BookingDto;
import com.workerconnect.enums.BookingStatus;
import com.workerconnect.kafka.KafkaProducer;
import com.workerconnect.model.*;
import com.workerconnect.repository.*;
import com.workerconnect.service.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    private final KafkaProducer producer;

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

        

        
        AgreementDto agreementDto = new AgreementDto(booking.getId(),
                                                    userId,
                                                dto.getWorkerId());

        BookingConfirmationKafkaDto  bookingConfirmationKafkaDto=new BookingConfirmationKafkaDto(
            bookingNumber,user.getEmail(),user.getFullName(),worker.getEmail(),worker.getFullName());
        
        producer.sendMessage("booking-agreement", agreementDto);

        producer.sendMessage("booking-confirmation", bookingConfirmationKafkaDto);

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
