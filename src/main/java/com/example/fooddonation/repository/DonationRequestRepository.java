// filename: src/main/java/com/example/fooddonation/repository/DonationRequestRepository.java
package com.example.fooddonation.repository;

import com.example.fooddonation.model.DonationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {
    List<DonationRequest> findByRequesterId(Long requesterId);
    List<DonationRequest> findByDonationId(Long donationId);
}