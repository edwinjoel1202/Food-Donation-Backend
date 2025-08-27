package com.example.fooddonation.controller;

import com.example.fooddonation.dto.UserDTO;
import com.example.fooddonation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Simple controller that exposes the current user's profile.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Returns the currently authenticated user's profile.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // AuthService sets username as email
        UserDTO dto = userService.getByEmail(email); // returns UserDTO
        return ResponseEntity.ok(dto);
    }
}
