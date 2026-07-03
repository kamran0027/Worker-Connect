package com.workerconnect.repository;

import com.workerconnect.enums.AgreementStatus;
import com.workerconnect.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    Optional<Agreement> findByBookingId(Long bookingId);
    List<Agreement> findByStatus(AgreementStatus status);
    long countByStatus(AgreementStatus status);
}
