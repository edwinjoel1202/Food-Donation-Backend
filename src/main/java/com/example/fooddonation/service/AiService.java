// filename: src/main/java/com/example/fooddonation/service/AiService.java
package com.example.fooddonation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * AI wrapper for calling Gemini / Generative API via HTTP.
 *
 * This version attempts multiple payload variants if the endpoint is strict about field names.
 */
@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.gemini.endpoint:}")
    private String geminiEndpoint;

    @Value("${ai.gemini.api_key:}")
    private String geminiApiKey;

    /**
     * A resilient HTTP-based generator that posts a prompt to the configured endpoint and returns generated text.
     * This method will attempt several payload shapes (no max tokens, snake_case max, camelCase max) and return the first
     * successful response.
     */
    private String generateWithHttp(String prompt, int maxTokens, String modelName) {
        if (prompt == null) prompt = "";

        if (geminiEndpoint == null || geminiEndpoint.isBlank()) {
            logger.warn("Gemini endpoint not configured (ai.gemini.endpoint). Returning fallback message.");
            return "AI not configured (no endpoint).";
        }
        String urlBase = geminiEndpoint.trim();

        // --- Start of Corrected Payload Logic ---

        // 1. Create the base payload with the "contents"
        Map<String, Object> baseBody = new LinkedHashMap<>();
        Map<String, Object> contentObj = new LinkedHashMap<>();
        contentObj.put("role", "user");
        contentObj.put("parts", List.of(Map.of("text", prompt)));
        baseBody.put("contents", List.of(contentObj));

        // 2. Create the payload with the CORRECTLY structured generationConfig
        Map<String, Object> bodyWithConfig = new LinkedHashMap<>(baseBody);

        // Create the main generationConfig map
        Map<String, Object> genConfig = new LinkedHashMap<>();
        genConfig.put("maxOutputTokens", Math.max(1, maxTokens));

        // *** THIS IS THE FIX: Explicitly disable the "Thinking" feature ***
        Map<String, Object> thinkingConfig = Map.of("thinkingBudget", 0);
        genConfig.put("thinkingConfig", thinkingConfig);

        bodyWithConfig.put("generationConfig", genConfig);

        List<String> candidateJsons = new ArrayList<>();
        try {
            // We only need one variant now with the correct configuration
            candidateJsons.add(objectMapper.writeValueAsString(bodyWithConfig));
        } catch (JsonProcessingException e) {
            logger.error("Failed to prepare JSON payload: {}", e.getMessage(), e);
            return "Error preparing AI request: " + e.getMessage();
        }

        // --- End of Corrected Payload Logic ---

        String url = urlBase + (urlBase.contains("?") ? "&" : "?") + "key=" + geminiApiKey.trim();
        HttpRequest.Builder baseReqBuilder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        String lastResponseBody = null;
        int lastStatus = -1;

        // We only have one correct payload to try now
        String reqJson = candidateJsons.get(0);
        try {
            HttpRequest req = baseReqBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                    .build();

            logger.debug("Sending AI request to {} (len={})", url, reqJson.length());
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            lastStatus = resp.statusCode();
            lastResponseBody = resp.body();

            if (lastStatus >= 200 && lastStatus < 300) {
                try {
                    JsonNode root = objectMapper.readTree(lastResponseBody);
                    String extracted = extractBestTextFromResponse(root);
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted.trim();
                    } else {
                        return lastResponseBody;
                    }
                } catch (Exception e) {
                    logger.warn("Successful status but failed parsing JSON. Returning raw body. parseErr={}", e.getMessage());
                    return lastResponseBody;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Network error sending AI request: {}", e.getMessage(), e);
            lastResponseBody = e.getMessage();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unexpected error on AI request: {}", e.getMessage(), e);
            lastResponseBody = e.getMessage();
        }

        String msg = "Error from AI service: Request failed. Status: HTTP " + lastStatus + ". Response: " + (lastResponseBody == null ? "no response" : lastResponseBody);
        logger.error(msg);
        return msg;
    }

    // helper to trim long strings for debug logs
    private String trimForLog(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String extractBestTextFromResponse(JsonNode root) {
        if (root == null) return null;

        if (root.has("candidates") && root.get("candidates").isArray()) {
            for (JsonNode candidate : root.get("candidates")) {
                if (candidate.has("content")) {
                    JsonNode content = candidate.get("content");
                    if (content.has("text")) return content.get("text").asText();
                    if (content.has("parts") && content.get("parts").isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode p : content.get("parts")) {
                            if (p.has("text")) sb.append(p.get("text").asText());
                            else if (p.isTextual()) sb.append(p.asText());
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                }
                if (candidate.has("output")) {
                    JsonNode out = candidate.get("output");
                    if (out.isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode el : out) {
                            if (el.isTextual()) sb.append(el.asText());
                            else if (el.has("text")) sb.append(el.get("text").asText());
                        }
                        if (sb.length() > 0) return sb.toString();
                    } else if (out.isTextual()) {
                        return out.asText();
                    }
                }
                if (candidate.has("text")) return candidate.get("text").asText();
            }
        }

        if (root.has("output")) {
            JsonNode out = root.get("output");
            if (out.isTextual()) return out.asText();
            if (out.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode el : out) {
                    if (el.isTextual()) sb.append(el.asText());
                    else if (el.has("text")) sb.append(el.get("text").asText());
                }
                if (sb.length() > 0) return sb.toString();
            }
        }
        if (root.has("text") && root.get("text").isTextual()) {
            return root.get("text").asText();
        }

        String recursive = findFirstTextRecursively(root);
        if (recursive != null && !recursive.isBlank()) return recursive;

        return null;
    }

    private String findFirstTextRecursively(JsonNode node) {
        if (node == null) return null;
        if (node.isTextual()) return node.asText();

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> f = fields.next();
                String key = f.getKey();
                JsonNode value = f.getValue();
                if ("text".equalsIgnoreCase(key) && value.isTextual()) {
                    return value.asText();
                }
                String found = findFirstTextRecursively(value);
                if (found != null && !found.isBlank()) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode el : node) {
                String found = findFirstTextRecursively(el);
                if (found != null && !found.isBlank()) return found;
            }
        }
        return null;
    }

    private String getModelName() {
        return "gemini-1.5-flash";
    }

    // --- (the rest of your public methods remain unchanged; keeping calculation & JSON parsing logic) ---

    public String categorizeFood(String name) {
        if (name == null || name.isEmpty()) return "uncategorized";
        String prompt = "Classify this food item into a category (cooked, raw, perishable, vegetables, fruits, dairy, grains, protein): \"" + name + "\". Return only the category.";
        return generateWithHttp(prompt, 10, getModelName()).trim().toLowerCase();
    }

    public String predictExpiry(String name) {
        String prompt = "Predict the number of days this food item can last before expiry: \"" + name + "\". Return only the number of days as an integer.";
        return generateWithHttp(prompt, 10, getModelName()).trim();
    }

    public Map<String, Object> calculateDetailedNutrition(String name, double quantity, String unit) {
        String prompt = "Provide detailed nutrition information for " + quantity + " " + unit + " of \"" + name + "\". " +
                "Include calories, protein (g), carbohydrates (g), fats (g), fiber (g), sugars (g), vitamins (e.g., vitamin C mg), minerals (e.g., iron mg). " +
                "Return only the JSON object with keys like 'calories', 'protein', etc. without any other text or code blocks.";
        String response = generateWithHttp(prompt, 250, getModelName()).trim();

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
            logger.error("Failed to parse nutrition JSON from AI response.", e);
            return Map.of("error", "Failed to parse nutrition info", "rawResponse", response);
        }
    }

    public String chatResponse(String message) {
        String prompt = "You are a helpful assistant for a food donation app. Respond to: " + message;
        return generateWithHttp(prompt, 200, getModelName()).trim();
    }

    public String detectAllergens(String name, String description) {
        String descPart = (description != null && !description.isEmpty()) ?
                " with description: \"" + description + "\"" : "";
        String prompt = "List potential allergens in the food item \"" + name + "\"" + descPart + ". " +
                "Common allergens: nuts, dairy, gluten, shellfish, eggs, soy, etc. Return a comma-separated list or 'none' if no known allergens.";
        return generateWithHttp(prompt, 50, getModelName()).trim();
    }

    public String suggestRecipes(String name, int servings) {
        String prompt = "Suggest 3 simple recipes using \"" + name + "\" as the main ingredient, for " + servings + " servings. " +
                "For each recipe, include title, ingredients list, and short instructions. Return as formatted text.";
        return generateWithHttp(prompt, 400, getModelName()).trim();
    }

    public String getStorageTips(String name) {
        String prompt = "Provide tips on how to store \"" + name + "\" to extend its shelf life. Include temperature, packaging, and duration. Return as bullet points.";
        return generateWithHttp(prompt, 300, getModelName()).trim();
    }

    public Map<String, Object> calculateConsumeRatio(String name, double quantity, String unit) {
        Map<String, Object> out = new LinkedHashMap<>();
        String rawUnit = unit == null ? "" : unit.toLowerCase().trim();
        String item = name == null ? "food" : name;
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
                count = quantity;
            } else if (rawUnit.contains("piece") || rawUnit.contains("pcs") || rawUnit.contains("pc") || rawUnit.contains("count")) {
                count = quantity;
            } else {
                String n = item.toLowerCase();
                if (n.contains("rice") || n.contains("wheat") || n.contains("flour")) {
                    grams = quantity * 1000.0;
                } else if (n.contains("milk") || n.contains("juice") || n.contains("curd") || n.contains("soup")) {
                    milliliters = quantity * 1000.0;
                } else {
                    count = quantity;
                }
            }
        } catch (Exception e) {
            grams = -1;
            milliliters = -1;
            count = quantity;
        }

        List<Map<String, Object>> variants = new ArrayList<>();
        if (grams >= 0) {
            double smallServing = 150.0;
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
            double pieces = (count >= 0) ? count : quantity;
            long smallPeople = (long) Math.floor(pieces / 1.0);
            long avgPeople = (long) Math.floor(pieces / 2.0);
            long largePeople = (long) Math.floor(pieces / 3.0);
            variants.add(Map.of("label", "1 piece per person", "persons", smallPeople, "piecesPerPerson", 1));
            variants.add(Map.of("label", "2 pieces per person", "persons", avgPeople, "piecesPerPerson", 2));
            variants.add(Map.of("label", "3 pieces per person", "persons", largePeople, "piecesPerPerson", 3));
            out.put("type", "count");
            out.put("total_count", pieces);
        }

        String explanation = String.format("Predictions for '%s' (%s %s). These are heuristic estimates: some people eat less (small), some eat average, some larger portions. Use the prediction that best matches your recipient group's appetite.", item, quantity, unit);
        out.put("variants", variants);
        out.put("explanation", explanation);
        out.put("input", Map.of("name", item, "quantity", quantity, "unit", unit));

        try {
            String apiKey = (geminiApiKey == null || geminiApiKey.isBlank()) ? System.getenv("GEMINI_API_KEY") : geminiApiKey;
            if (apiKey != null && !apiKey.isBlank()) {
                String prompt = "Given the donated item: \"" + item + "\" with quantity " + quantity + " " + unit + ", provide a concise explanation (1-2 sentences) of how many people might be fed and why. Also propose three portion sizes (small/average/large) in grams or ml or pieces depending on unit.";
                String aiResp = generateWithHttp(prompt, 200, getModelName());
                out.put("aiNote", aiResp);
            } else {
                out.put("aiNote", "AI not configured - using deterministic heuristic.");
            }
        } catch (Exception e) {
            // ignore AI failure, keep deterministic result
        }

        return out;
    }
}
