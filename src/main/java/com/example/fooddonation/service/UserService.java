// filename: src/main/java/com/example/fooddonation/service/UserService.java
package com.example.fooddonation.service;

import com.example.fooddonation.dto.UserDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    @Transactional(readOnly = true)
    public UserDTO getByEmail(String email) {
        var u = userRepo.findByEmail(email).orElseThrow(() -> new ApiException("User not found"));
        return new UserDTO(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getAddress(),
                u.getLat(),
                u.getLng(),
                u.getCity(),
                u.getState(),
                u.getPostalCode(),
                u.getCountry()
        );
    }
}