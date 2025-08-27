// filename: src/main/java/com/example/fooddonation/service/AiService.java
package com.example.fooddonation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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

    public String calculateNutrition(String name, double quantity, String unit) {
        String prompt = "Estimate the total calories for " + quantity + " " + unit + " of \"" + name + "\". Return only the number of calories.";
        return generateWithGemini(prompt, 10).trim();
    }

    public String chatResponse(String message) {
        String prompt = "You are a helpful assistant for a food donation app. Respond to: " + message;
        return generateWithGemini(prompt, 200).trim();
    }
}