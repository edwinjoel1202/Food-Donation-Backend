// filename: src/main/java/com/example/fooddonation/repository/UserRepository.java
package com.example.fooddonation.repository;

import com.example.fooddonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}