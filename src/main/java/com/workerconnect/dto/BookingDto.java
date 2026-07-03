package com.workerconnect.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingDto {
    @NotNull(message = "Worker is required")
    private Long workerId;

    @NotNull(message = "Booking date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime bookingTime;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "Completion date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate completionDate;

    @NotBlank(message = "Work description is required")
    private String workDescription;

    private String serviceType; // HOURLY, DAILY, FIXED
    private BigDecimal amount;
}
