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
        return generateWithGemini(prompt, 300).trim();
    }

    /**
     * Calculate how many people can consume the donated food,
     * returning multiple estimates (small eaters / average / large eaters).
     *
     * The method is deterministic and does not require external AI. If Gemini is configured,
     * we include the AI text as an optional 'aiNote'.
     */
    public Map<String, Object> calculateConsumeRatio(String name, double quantity, String unit) {
        Map<String, Object> out = new LinkedHashMap<>();
        String rawUnit = unit == null ? "" : unit.toLowerCase().trim();
        String item = name == null ? "food" : name;

        // Normalize quantity to a simple unit type: grams, milliliters, count
        double grams = -1;
        double milliliters = -1;
        double count = -1;

        try {
            if (rawUnit.contains("kg") || rawUnit.contains("kilogram")) {
                grams = quantity * 1000.0;
            } else if (rawUnit.contains("g") || rawUnit.contains("gram")) {
                grams = quantity;
            } else if (rawUnit.contains("l") || rawUnit.contains("liter") || rawUnit.contains("litre")) {
                milliliters = quantity * 1000.0;
            } else if (rawUnit.contains("ml") || rawUnit.contains("milliliter")) {
                milliliters = quantity;
            } else if (rawUnit.contains("plate") || rawUnit.contains("plates")) {
                // treat as count of plates
                count = quantity;
            } else if (rawUnit.contains("piece") || rawUnit.contains("pcs") || rawUnit.contains("pc") || rawUnit.contains("count")) {
                count = quantity;
            } else {
                // unknown unit: attempt to guess from name (rice, chapati, apple, etc.)
                String n = item.toLowerCase();
                if (n.contains("rice") || n.contains("wheat") || n.contains("flour")) {
                    grams = quantity * 1000.0; // assume user used kg value but didn't specify
                } else if (n.contains("milk") || n.contains("juice") || n.contains("curd") || n.contains("soup")) {
                    milliliters = quantity * 1000.0;
                } else {
                    // fallback treat as count
                    count = quantity;
                }
            }
        } catch (Exception e) {
            // fallbacks below
            grams = -1; milliliters = -1; count = quantity;
        }

        List<Map<String, Object>> variants = new ArrayList<>();

        // For grains / solid food measured in grams
        if (grams >= 0) {
            // serving sizes in grams for small/average/large eater
            double smallServing = 150.0; // grams
            double avgServing = 200.0;
            double largeServing = 300.0;

            double smallPeople = Math.floor(grams / smallServing);
            double avgPeople = Math.floor(grams / avgServing);
            double largePeople = Math.floor(grams / largeServing);

            variants.add(Map.of("label", "Small eater (~150 g)", "persons", (long) smallPeople, "serving_g", smallServing));
            variants.add(Map.of("label", "Average eater (~200 g)", "persons", (long) avgPeople, "serving_g", avgServing));
            variants.add(Map.of("label", "Large eater (~300 g)", "persons", (long) largePeople, "serving_g", largeServing));
            out.put("type", "grams");
            out.put("total_grams", grams);

        } else if (milliliters >= 0) {
            // liquids
            double smallMl = 150.0;
            double avgMl = 250.0;
            double largeMl = 350.0;

            double smallPeople = Math.floor(milliliters / smallMl);
            double avgPeople = Math.floor(milliliters / avgMl);
            double largePeople = Math.floor(milliliters / largeMl);

            variants.add(Map.of("label", "Small serving (~150 ml)", "persons", (long) smallPeople, "serving_ml", smallMl));
            variants.add(Map.of("label", "Average (~250 ml)", "persons", (long) avgPeople, "serving_ml", avgMl));
            variants.add(Map.of("label", "Large (~350 ml)", "persons", (long) largePeople, "serving_ml", largeMl));
            out.put("type", "milliliters");
            out.put("total_ml", milliliters);

        } else {
            // treat as countable pieces or plates
            double pieces = (count >= 0) ? count : quantity;
            // assume 1 piece per person (small), 2 pieces average, 3 pieces large
            long smallPeople = (long) Math.floor(pieces / 1.0);
            long avgPeople = (long) Math.floor(pieces / 2.0);
            long largePeople = (long) Math.floor(pieces / 3.0);

            variants.add(Map.of("label", "1 piece per person", "persons", smallPeople, "piecesPerPerson", 1));
            variants.add(Map.of("label", "2 pieces per person", "persons", avgPeople, "piecesPerPerson", 2));
            variants.add(Map.of("label", "3 pieces per person", "persons", largePeople, "piecesPerPerson", 3));
            out.put("type", "count");
            out.put("total_count", pieces);
        }

        // Explanation text
        String explanation = String.format("Predictions for '%s' (%s %s). These are heuristic estimates: some people eat less (small), some eat average, some larger portions. Use the prediction that best matches your recipient group's appetite.", item, quantity, unit);
        out.put("variants", variants);
        out.put("explanation", explanation);
        out.put("input", Map.of("name", item, "quantity", quantity, "unit", unit));

        // If an AI is configured, optionally ask it for a short text note (non-blocking)
        if (aiEndpoint != null && !aiEndpoint.isBlank() && aiKey != null && !aiKey.isBlank()) {
            try {
                String prompt = "Given the donated item: \"" + item + "\" with quantity " + quantity + " " + unit + ", provide a concise explanation (1-2 sentences) of how many people might be fed and why. Also propose three portion sizes (small/average/large) in grams or ml or pieces depending on unit.";
                String aiResp = generateWithGemini(prompt, 200);
                out.put("aiNote", aiResp);
            } catch (Exception e) {
                // ignore AI failure, keep deterministic result
            }
        } else {
            out.put("aiNote", "AI not configured - using deterministic heuristic.");
        }

        return out;
    }
}