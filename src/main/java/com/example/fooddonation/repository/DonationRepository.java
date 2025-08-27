// filename: src/main/java/com/example/fooddonation/repository/DonationRepository.java
package com.example.fooddonation.repository;

import com.example.fooddonation.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByStatus(String status);
    List<Donation> findByCreatedById(Long userId);
}