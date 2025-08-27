// filename: src/main/java/com/example/fooddonation/controller/AiController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Exposes AI endpoints for categorization / expiry prediction / nutrition.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @GetMapping("/categorize")
    public ResponseEntity<?> categorize(@RequestParam("name") String name) {
        String category = aiService.categorizeFood(name);
        return ResponseEntity.ok(Map.of("category", category));
    }

    @GetMapping("/predict-expiry")
    public ResponseEntity<?> predictExpiry(@RequestParam("name") String name) {
        String expiryDays = aiService.predictExpiry(name);
        return ResponseEntity.ok(Map.of("expiryDays", expiryDays));
    }

    @GetMapping("/nutrition")
    public ResponseEntity<?> nutrition(@RequestParam("name") String name, @RequestParam("quantity") double quantity, @RequestParam("unit") String unit) {
        String calories = aiService.calculateNutrition(name, quantity, unit);
        return ResponseEntity.ok(Map.of("calories", calories));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String response = aiService.chatResponse(message);
        return ResponseEntity.ok(Map.of("response", response));
    }
}