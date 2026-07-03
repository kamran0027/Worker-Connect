package com.workerconnect.service;

import com.workerconnect.enums.WorkerStatus;
import com.workerconnect.model.Category;
import com.workerconnect.model.User;
import com.workerconnect.model.Worker;
import com.workerconnect.repository.CategoryRepository;
import com.workerconnect.repository.WorkerRepository;
import com.workerconnect.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    public Worker findById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
    }

    public Worker findByUserId(Long userId) {
        return workerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
    }

    public List<Worker> searchWorkers(String city, String profession, BigDecimal minPrice, BigDecimal maxPrice, Double minRating) {
        return workerRepository.searchWorkers(city, profession, minPrice, maxPrice, minRating);
    }

    public List<Worker> findApprovedWorkers() {
        return workerRepository.findByStatus(WorkerStatus.APPROVED);
    }

    public List<Worker> findPendingWorkers() {
        return workerRepository.findByStatus(WorkerStatus.PENDING_APPROVAL);
    }

    public List<Worker> findAllWorkers() {
        return workerRepository.findAll();
    }

    public List<Worker> findTopRatedWorkers() {
        return workerRepository.findTopRatedWorkers();
    }

    public List<Worker> findByCategoryId(Long categoryId) {
        return workerRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Worker updateProfile(Long workerId, Worker updated, Long categoryId) {
        Worker w = findById(workerId);
        w.setFullName(updated.getFullName());
        w.setPhone(updated.getPhone());
        w.setAddress(updated.getAddress());
        w.setCity(updated.getCity());
        w.setState(updated.getState());
        w.setProfession(updated.getProfession());
        w.setSpecialization(updated.getSpecialization());
        w.setExperienceYears(updated.getExperienceYears());
        w.setSkills(updated.getSkills());
        w.setDescription(updated.getDescription());
        w.setServiceArea(updated.getServiceArea());
        w.setHourlyRate(updated.getHourlyRate());
        w.setDailyRate(updated.getDailyRate());
        w.setFixedRate(updated.getFixedRate());
        w.setAvailable(updated.isAvailable());
        w.setWorkingDays(updated.getWorkingDays());
        w.setWorkingHours(updated.getWorkingHours());

        if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            w.setCategory(cat);
        }
        return workerRepository.save(w);
    }

    @Transactional
    public Worker updateProfileImage(Long workerId, MultipartFile file) throws IOException {
        Worker w = findById(workerId);
        String path = fileStorageService.storeProfileImage(file);
        w.setProfileImage(path);
        User user = w.getUser();
        user.setProfileImage(path);
        return workerRepository.save(w);
    }

    @Transactional
    public Worker uploadDocuments(Long workerId, MultipartFile aadhaar, MultipartFile pan, MultipartFile cert) throws IOException {
        Worker w = findById(workerId);
        if (aadhaar != null && !aadhaar.isEmpty()) w.setAadhaarCard(fileStorageService.storeDocument(aadhaar));
        if (pan != null && !pan.isEmpty()) w.setPanCard(fileStorageService.storeDocument(pan));
        if (cert != null && !cert.isEmpty()) w.setExperienceCertificate(fileStorageService.storeDocument(cert));
        return workerRepository.save(w);
    }

    @Transactional
    public Worker toggleAvailability(Long workerId) {
        Worker w = findById(workerId);
        w.setAvailable(!w.isAvailable());
        return workerRepository.save(w);
    }

    @Transactional
    public void approveWorker(Long workerId) {
        Worker w = findById(workerId);
        w.setStatus(WorkerStatus.APPROVED);
        workerRepository.save(w);
        emailService.sendWorkerApprovalEmail(w.getEmail(), w.getFullName());
    }

    @Transactional
    public void rejectWorker(Long workerId, String reason) {
        Worker w = findById(workerId);
        w.setStatus(WorkerStatus.REJECTED);
        workerRepository.save(w);
        emailService.sendWorkerRejectionEmail(w.getEmail(), w.getFullName(), reason);
    }

    @Transactional
    public void suspendWorker(Long workerId) {
        Worker w = findById(workerId);
        w.setStatus(WorkerStatus.SUSPENDED);
        w.getUser().setEnabled(false);
        workerRepository.save(w);
    }

    @Transactional
    public void deleteWorker(Long workerId) {
        workerRepository.deleteById(workerId);
    }

    @Transactional
    public void updateRating(Long workerId, Double newRating, Integer totalReviews) {
        Worker w = findById(workerId);
        w.setAverageRating(newRating);
        w.setTotalReviews(totalReviews);
        workerRepository.save(w);
    }
}
