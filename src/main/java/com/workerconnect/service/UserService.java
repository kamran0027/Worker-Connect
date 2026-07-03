package com.workerconnect.service;

import com.workerconnect.model.User;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String address, String city, String state) {
        User user = findById(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setCity(city);
        user.setState(state);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = findById(userId);
        String path = fileStorageService.storeProfileImage(file);
        user.setProfileImage(path);
        return userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }
}
