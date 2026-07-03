package com.workerconnect.model;

import com.workerconnect.enums.AgreementStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // User details snapshot
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userAddress;

    // Worker details snapshot
    private String workerName;
    private String workerEmail;
    private String workerPhone;
    private String workerProfession;

    @Column(columnDefinition = "TEXT")
    private String serviceDescription;

    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate completionDate;

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @Enumerated(EnumType.STRING)
    private AgreementStatus status = AgreementStatus.PENDING;

    private boolean userSigned = false;
    private boolean workerSigned = false;
    private LocalDateTime userSignedAt;
    private LocalDateTime workerSignedAt;

    private String agreementFilePath;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
