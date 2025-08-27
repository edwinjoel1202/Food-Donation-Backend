// filename: src/main/java/com/example/fooddonation/controller/AuthController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.dto.AuthRequestDTO;
import com.example.fooddonation.dto.AuthResponseDTO;
import com.example.fooddonation.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid AuthRequestDTO dto) {
        AuthResponseDTO resp = authService.register(dto);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO dto) {
        AuthResponseDTO resp = authService.login(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(resp);
    }
}