package com.workerconnect.service;

import com.workerconnect.enums.AgreementStatus;
import com.workerconnect.enums.BookingStatus;
import com.workerconnect.model.Agreement;
import com.workerconnect.model.Booking;
import com.workerconnect.repository.AgreementRepository;
import com.workerconnect.util.FileStorageService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementRepository agreementRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final FileStorageService fileStorageService;

    public Agreement findById(Long id) {
        return agreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
    }

    public Agreement findByBookingId(Long bookingId) {
        return agreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
    }

    public List<Agreement> getAllAgreements() {
        return agreementRepository.findAll();
    }

    @Transactional
    public Agreement userSign(Long agreementId) {
        Agreement a = findById(agreementId);
        Booking b=a.getBooking();
        if(b.getStatus()==BookingStatus.PENDING || b.getStatus()==BookingStatus.REJECTED){
            throw new RuntimeException("Booking is not in a valid state for signing the agreement");
        }
        a.setUserSigned(true);
        a.setUserSignedAt(LocalDateTime.now());
        if (a.isWorkerSigned()) {
            a.setStatus(AgreementStatus.BOTH_SIGNED);
        } else {
            a.setStatus(AgreementStatus.USER_SIGNED);
        }
        return agreementRepository.save(a);
    }

    @Transactional
    public Agreement workerSign(Long agreementId) {
        Agreement a = findById(agreementId);
        Booking b=a.getBooking();
        if(b.getStatus()==BookingStatus.PENDING || b.getStatus()==BookingStatus.REJECTED){
            throw new RuntimeException("Booking is not in a valid state for signing the agreement");
        }
        a.setWorkerSigned(true);
        a.setWorkerSignedAt(LocalDateTime.now());
        if (a.isUserSigned()) {
            a.setStatus(AgreementStatus.BOTH_SIGNED);
        } else {
            a.setStatus(AgreementStatus.WORKER_SIGNED);
        }
        return agreementRepository.save(a);
    }

    public byte[] generateAgreementPdf(Long agreementId) throws DocumentException, IOException {
        Agreement agreement = findById(agreementId);
        Booking booking = agreement.getBooking();
        return pdfGeneratorService.generateAgreement(agreement, booking);
    }
}
