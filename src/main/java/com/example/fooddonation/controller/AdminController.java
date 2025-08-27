// filename: src/main/java/com/example/fooddonation/controller/AdminController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.dto.GenericResponseDTO;
import com.example.fooddonation.model.User;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Minimal admin controller. Add more admin operations.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/disable-user/{id}")
    public ResponseEntity<GenericResponseDTO> disableUser(@PathVariable Long id) {
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setEnabled(false);
        userRepo.save(u);
        return ResponseEntity.ok(new GenericResponseDTO(true, "User disabled"));
    }
}