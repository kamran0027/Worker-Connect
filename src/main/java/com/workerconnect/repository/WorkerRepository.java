package com.workerconnect.repository;

import com.workerconnect.enums.WorkerStatus;
import com.workerconnect.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByUserId(Long userId);
    Optional<Worker> findByEmail(String email);

    List<Worker> findByStatus(WorkerStatus status);
    List<Worker> findByStatusAndAvailable(WorkerStatus status, boolean available);

    @Query("SELECT w FROM Worker w WHERE w.status = 'APPROVED' AND " +
           "(:city IS NULL OR LOWER(w.city) = LOWER(:city)) AND " +
           "(:profession IS NULL OR LOWER(w.profession) LIKE LOWER(CONCAT('%', :profession, '%'))) AND " +
           "(:minPrice IS NULL OR w.dailyRate >= :minPrice) AND " +
           "(:maxPrice IS NULL OR w.dailyRate <= :maxPrice) AND " +
           "(:minRating IS NULL OR w.averageRating >= :minRating)")
    List<Worker> searchWorkers(
            @Param("city") String city,
            @Param("profession") String profession,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating);

    @Query("SELECT w FROM Worker w WHERE w.status = 'APPROVED' AND w.category.id = :categoryId")
    List<Worker> findByCategoryId(@Param("categoryId") Long categoryId);

    long countByStatus(WorkerStatus status);

    @Query("SELECT w FROM Worker w WHERE w.status = 'APPROVED' ORDER BY w.averageRating DESC")
    List<Worker> findTopRatedWorkers();
}
