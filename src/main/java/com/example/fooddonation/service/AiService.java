// filename: src/main/java/com/example/fooddonation/service/AiService.java
package com.example.fooddonation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI wrapper for Gemini API.
 */
@Service
public class AiService {

    @Value("${ai.gemini.endpoint}")
    private String aiEndpoint;

    @Value("${ai.gemini.api_key}")
    private String aiKey;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String generateWithGemini(String prompt, int maxTokens) {
        String url = aiEndpoint + "?key=" + aiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of("maxOutputTokens", maxTokens);
        Map<String, Object> body = Map.of("contents", List.of(content), "generationConfig", generationConfig);

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = rest.postForEntity(url, request, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                List<Map> candidates = (List<Map>) resp.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map contentMap = (Map) candidates.get(0).get("content");
                    if (contentMap != null) {
                        List<Map> parts = (List<Map>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    public String categorizeFood(String name) {
        if (name == null || name.isEmpty()) return "uncategorized";
        String prompt = "Classify this food item into a category (cooked, raw, perishable, vegetables, fruits, dairy, grains, protein): \"" + name + "\". Return only the category.";
        return generateWithGemini(prompt, 10).trim().toLowerCase();
    }

    public String predictExpiry(String name) {
        String prompt = "Predict the number of days this food item can last before expiry: \"" + name + "\". Return only the number of days as an integer.";
        return generateWithGemini(prompt, 10).trim();
    }

    public Map<String, Object> calculateDetailedNutrition(String name, double quantity, String unit) {
        String prompt = "Provide detailed nutrition information for " + quantity + " " + unit + " of \"" + name + "\". " +
                "Include calories, protein (g), carbohydrates (g), fats (g), fiber (g), sugars (g), vitamins (e.g., vitamin C mg), minerals (e.g., iron mg). " +
                "Return only the JSON object with keys like 'calories', 'protein', etc. without any other text or code blocks.";
        String response = generateWithGemini(prompt, 200).trim();

        // Strip any potential markdown code blocks
        if (response.startsWith("```json")) {
            response = response.substring(7).trim();
        } else if (response.startsWith("```")) {
            response = response.substring(3).trim();
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3).trim();
        }

        try {
            return objectMapper.readValue(response, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Map.of("error", "Failed to parse nutrition info", "rawResponse", response);
        }
    }

    public String chatResponse(String message) {
        String prompt = "You are a helpful assistant for a food donation app. Respond to: " + message;
        return generateWithGemini(prompt, 200).trim();
    }

    // New method for allergen detection
    public String detectAllergens(String name, String description) {
        String descPart = (description != null && !description.isEmpty()) ? " with description: \"" + description + "\"" : "";
        String prompt = "List potential allergens in the food item \"" + name + "\"" + descPart + ". " +
                "Common allergens: nuts, dairy, gluten, shellfish, eggs, soy, etc. Return a comma-separated list or 'none' if no known allergens.";
        return generateWithGemini(prompt, 50).trim();
    }

    // New method for recipe suggestions
    public String suggestRecipes(String name, int servings) {
        String prompt = "Suggest 3 simple recipes using \"" + name + "\" as the main ingredient, for " + servings + " servings. " +
                "For each recipe, include title, ingredients list, and short instructions. Return as formatted text.";
        return generateWithGemini(prompt, 300).trim();
    }

    // New method for storage tips
    public String getStorageTips(String name) {
        String prompt = "Provide tips on how to store \"" + name + "\" to extend its shelf life. Include temperature, packaging, and duration. Return as bullet points.";
        return generateWithGemini(prompt, 150).trim();
    }
}