package com.bootcoding.selector.service;

import com.bootcoding.selector.dto.GeminiPrompt;
import com.bootcoding.selector.dto.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeminiService {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    // Constructor with ResourceLoader and ObjectMapper
    public GeminiService(
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper
    ) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public List<String> processTopics() throws IOException {
        Topics topics = readTopicsFile();
        List<String> responses = new ArrayList<>();

        for (String topic : topics.getTopics()) {
            GeminiPrompt prompt = createPrompt(topic, "java");
            String response = sendPrompt(prompt);
            responses.add(response);
        }

        return responses;
    }

    private Topics readTopicsFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:java.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, Topics.class);
        }
    }

    private GeminiPrompt createPrompt(String topic, String subject) {
        // Create prompt
        GeminiPrompt prompt = new GeminiPrompt();

        // Create system instruction
        GeminiPrompt.SystemInstruction systemInstruction = new GeminiPrompt.SystemInstruction();
        GeminiPrompt.Part systemPart = new GeminiPrompt.Part();
        systemPart.setText("You are a technical interviewer. This is the json format in which I want my question in   \"{ \\\"Subject\\\": \\\"\\\", \\\"question\\\": \\\"\\\", \\\"idealAnswer\\\": \\\" Should be Precise and Explanation\\\", \" +\n" +
                "                        \"\\\"category\\\": \\\"\\\", \\\"subCategory\\\": \\\"\\\", \\\"topic\\\": \\\"\\\", \\\"subTopic\\\": \\\"\\\", \" +\n" +
                "                        \"\\\"DifficultyLevel\\\": \\\"\\\", \\\"Level\\\": \\\"1 to 5\\\" }\"");
        systemInstruction.setParts(Collections.singletonList(systemPart));
        prompt.setSystemInstruction(systemInstruction);

        // Create content
        GeminiPrompt.Content content = new GeminiPrompt.Content();
        GeminiPrompt.Part contentPart = new GeminiPrompt.Part();
        String promptText = String.format(
                "Ensure the question type varies among these categories: 'what' (definition), " +
                        "'how' (process), 'why' (reasoning), 'when' (scenario), 'compare' (difference). " +
                        "Give me interview question from this topic '%s' in '%s' should be in this format : ",
                topic, subject
        );
        contentPart.setText(promptText);
        content.setParts(Collections.singletonList(contentPart));
        prompt.setContents(Collections.singletonList(content));

        return prompt;
    }

    public String sendPrompt(GeminiPrompt prompt) throws IOException {
        String jsonPayload = objectMapper.writeValueAsString(prompt);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl + "?key=" + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    throw new IOException("Unexpected response code: " + response.getCode());
                }
                return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            });
        }
    }
}