package com.workerconnect.config;

import com.workerconnect.enums.Role;
import com.workerconnect.model.Category;
import com.workerconnect.model.User;
import com.workerconnect.repository.CategoryRepository;
import com.workerconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initAdmin();
        initCategories();
    }

    private void initAdmin() {
        if (!userRepository.existsByEmail("admin@workerconnect.com")) {
            User admin = User.builder()
                    .fullName("Super Admin")
                    .email("admin@workerconnect.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .emailVerified(true)
                    .city("Mumbai")
                    .state("Maharashtra")
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin@workerconnect.com / Admin@123");
        }
    }

    private void initCategories() {
        List<String> categories = List.of(
                "Labour", "Cook", "Carpenter", "Electrician", "Plumber",
                "Painter", "House Cleaner", "Mechanic", "Driver", "Gardener",
                "AC Technician", "Mason", "Welder", "Others"
        );

        for (String name : categories) {
            if (!categoryRepository.existsByName(name)) {
                Category category = Category.builder()
                        .name(name)
                        .description("Professional " + name + " services")
                        .active(true)
                        .build();
                categoryRepository.save(category);
            }
        }
        log.info("Categories initialized");
    }
}
