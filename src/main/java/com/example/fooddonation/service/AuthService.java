// filename: src/main/java/com/example/fooddonation/service/AuthService.java
package com.example.fooddonation.service;

import com.example.fooddonation.config.JwtUtil;
import com.example.fooddonation.dto.AuthRequestDTO;
import com.example.fooddonation.dto.AuthResponseDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.model.Role;
import com.example.fooddonation.model.User;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDTO register(AuthRequestDTO dto) {
        if (dto.getEmail() == null || dto.getPassword() == null) throw new ApiException("Email & password required");
        if (userRepo.existsByEmail(dto.getEmail())) throw new ApiException("Email already registered");

        Role role = Role.USER;
        if (dto.getRole() != null) {
            try {
                role = Role.valueOf(dto.getRole());
            } catch (Exception e) {
                role = Role.USER;
            }
        }
        User user = new User(dto.getName(), dto.getEmail(), passwordEncoder.encode(dto.getPassword()), role);

        // set address/location if provided
        user.setAddress(dto.getAddress());
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setPostalCode(dto.getPostalCode());
        user.setCountry(dto.getCountry());
        user.setLat(dto.getLat());
        user.setLng(dto.getLng());

        userRepo.save(user);
        // generate token
        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getAddress(),
                user.getLat(),
                user.getLng(),
                user.getCity(),
                user.getState(),
                user.getPostalCode(),
                user.getCountry()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(String email, String password) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new ApiException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new ApiException("Invalid credentials");
        UserDetails ud = loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud);
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getAddress(),
                user.getLat(),
                user.getLng(),
                user.getCity(),
                user.getState(),
                user.getPostalCode(),
                user.getCountry()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<SimpleGrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), auth);
    }
}