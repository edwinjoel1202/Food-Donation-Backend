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
        Map<String, Object> nutritionInfo = aiService.calculateDetailedNutrition(name, quantity, unit);
        return ResponseEntity.ok(nutritionInfo);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String response = aiService.chatResponse(message);
        return ResponseEntity.ok(Map.of("response", response));
    }

    // New endpoint for allergen detection
    @GetMapping("/allergens")
    public ResponseEntity<?> allergens(@RequestParam("name") String name, @RequestParam(value = "description", required = false) String description) {
        String allergens = aiService.detectAllergens(name, description);
        return ResponseEntity.ok(Map.of("allergens", allergens));
    }

    // New endpoint for recipe suggestions
    @GetMapping("/recipes")
    public ResponseEntity<?> recipes(@RequestParam("name") String name, @RequestParam(value = "servings", defaultValue = "4") int servings) {
        Object recipes = aiService.suggestRecipes(name, servings);
        return ResponseEntity.ok(Map.of("recipes", recipes));
    }

    // New endpoint for storage tips to extend shelf life
    @GetMapping("/storage-tips")
    public ResponseEntity<?> storageTips(@RequestParam("name") String name) {
        String tips = aiService.getStorageTips(name);
        return ResponseEntity.ok(Map.of("tips", tips));
    }
}