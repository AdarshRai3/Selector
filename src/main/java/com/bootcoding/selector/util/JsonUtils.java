package com.bootcoding.selector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractRelevantJson(String responseJson) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray()) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    jsonBuilder.append("[");

                    boolean first = true;
                    for (JsonNode part : parts) {
                        String text = part.path("text").asText("");
                        String cleanedJson = cleanupJsonText(text);

                        if (!cleanedJson.isEmpty() && !cleanedJson.equals("[]")) {
                            if (!first) {
                                jsonBuilder.append(",");
                            }
                            jsonBuilder.append(cleanedJson);
                            first = false;
                        }
                    }

                    jsonBuilder.append("]");
                    return jsonBuilder.toString();
                }
            }
            return "[]";
        } catch (Exception e) {
            logger.error("Error extracting JSON from response: {}", e.getMessage());
            return "[]";
        }
    }

    private String cleanupJsonText(String text) {
        // Remove markdown code blocks
        text = text.replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        // Remove surrounding brackets if present
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
        }

        // Ensure the text is valid JSON
        try {
            objectMapper.readTree(text);
            return text;
        } catch (Exception e) {
            logger.warn("Invalid JSON found in text: {}", text);
            return "";
        }
    }

    public String formatJsonResponse(String response) {
        try {
            // Parse the JSON to validate and pretty print it
            JsonNode jsonNode = objectMapper.readTree(response);

            // If it's not an array, wrap it in an array
            if (!response.trim().startsWith("[")) {
                return "[" + objectMapper.writeValueAsString(jsonNode) + "]";
            }

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            logger.error("Error formatting JSON response: {}", e.getMessage());
            return "[]";
        }
    }
}