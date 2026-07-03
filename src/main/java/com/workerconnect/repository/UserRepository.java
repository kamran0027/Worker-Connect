package com.workerconnect.repository;

import com.workerconnect.enums.Role;
import com.workerconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
    Optional<User> findByEmailVerificationToken(String token);
    List<User> findByRole(Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsers(String query);

    long countByRole(Role role);
}
