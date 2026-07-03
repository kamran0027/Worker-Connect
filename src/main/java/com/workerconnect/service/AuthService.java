package com.workerconnect.service;

import com.workerconnect.dto.UserRegistrationDto;
import com.workerconnect.dto.WorkerRegistrationDto;
import com.workerconnect.enums.Role;
import com.workerconnect.model.Category;
import com.workerconnect.model.User;
import com.workerconnect.model.Worker;
import com.workerconnect.repository.CategoryRepository;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .city(dto.getCity())
                .state(dto.getState())
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public Worker registerWorker(WorkerRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .city(dto.getCity())
                .state(dto.getState())
                .role(Role.ROLE_WORKER)
                .enabled(true)
                .emailVerified(true)
                .build();
        user = userRepository.save(user);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Worker worker = Worker.builder()
                .user(user)
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .category(category)
                .profession(dto.getProfession())
                .specialization(dto.getSpecialization())
                .experienceYears(dto.getExperienceYears())
                .skills(dto.getSkills())
                .description(dto.getDescription())
                .serviceArea(dto.getServiceArea())
                .hourlyRate(dto.getHourlyRate())
                .dailyRate(dto.getDailyRate())
                .fixedRate(dto.getFixedRate())
                .workingDays(dto.getWorkingDays())
                .workingHours(dto.getWorkingHours())
                .available(true)
                .build();
        return workerRepository.save(worker);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
        if (user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
