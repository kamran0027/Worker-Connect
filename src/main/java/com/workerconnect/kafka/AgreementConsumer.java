package com.workerconnect.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.workerconnect.dto.AgreementDto;
import com.workerconnect.enums.AgreementStatus;
import com.workerconnect.model.Agreement;
import com.workerconnect.model.Booking;
import com.workerconnect.model.User;
import com.workerconnect.model.Worker;
import com.workerconnect.repository.AgreementRepository;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.repository.WorkerRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor

public class AgreementConsumer {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    private final BookingRepository bookingRepository;

    private final AgreementRepository agreementRepository;

    @KafkaListener(topics = "booking-agreement")
    public void agreementBuild(AgreementDto dto){

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Worker worker = workerRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        Booking booking=bookingRepository.findById(dto.getBookingId())
                .orElseThrow(()-> new RuntimeException("Booking not found"));

        Agreement agreement=Agreement.builder()
                            .booking(booking)
                            .userName(user.getFullName())
                            .userEmail(user.getEmail())
                            .userPhone(user.getPhone())
                            .userAddress(user.getAddress())
                            .workerName(worker.getFullName())
                            .workerEmail(worker.getEmail())
                            .workerPhone(worker.getPhone())
                            .workerProfession(worker.getProfession())
                            .serviceDescription(booking.getWorkDescription())
                            .amount(booking.getAmount())
                            .startDate(booking.getStartDate())
                            .completionDate(booking.getCompletionDate())
                            .status(AgreementStatus.PENDING)
                            .build();
        agreementRepository.save(agreement);
                            
        System.out.println("**********************************************");
        System.out.println("kafka consumer ");
        System.out.println("Received agreement build request for booking: " + worker.getFullName() + " with user: " + user.getFullName());
        System.out.println("**********************************************");
    }

}
