package com.workerconnect.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkerRegistrationDto {
    // Personal Info
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    // Professional Info
    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Profession is required")
    private String profession;

    private String specialization;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    private String skills;
    private String description;
    private String serviceArea;

    // Pricing
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal fixedRate;

    // Availability
    private String workingDays;
    private String workingHours;
}
