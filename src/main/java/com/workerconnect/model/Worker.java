package com.workerconnect.model;

import com.workerconnect.enums.WorkerStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    // Personal Info
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String profileImage;

    // Professional Info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String profession;
    private String specialization;
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String serviceArea;

    // Pricing
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal fixedRate;

    // Availability
    private boolean available = true;
    private String workingDays;
    private String workingHours;

    // Documents
    private String aadhaarCard;
    private String panCard;
    private String experienceCertificate;

    // Status & Rating
    @Enumerated(EnumType.STRING)
    private WorkerStatus status = WorkerStatus.PENDING_APPROVAL;

    private Double averageRating = 0.0;
    private Integer totalReviews = 0;
    private Integer totalJobsCompleted = 0;

    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

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
