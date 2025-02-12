package com.bootcoding.selector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.bootcoding.selector.dto.GeminiPrompt;
import com.bootcoding.selector.dto.Topics;
import com.bootcoding.selector.entity.QuestionBank;
import com.bootcoding.selector.repository.QuestionBankRepository;
import com.bootcoding.selector.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final JsonUtils jsonUtils;
    private final QuestionBankRepository questionBankRepository;

    public GeminiService(ResourceLoader resourceLoader, ObjectMapper objectMapper, JsonUtils jsonUtils,
                         QuestionBankRepository questionBankRepository) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.jsonUtils = jsonUtils;
        this.questionBankRepository = questionBankRepository;
    }

    public String processTopics() throws IOException {
        Topics topics = readTopicsFile();
        StringBuilder allResponses = new StringBuilder();
        boolean isFirst = true;

        for (String topic : topics.getTopics()) {
            try {
                GeminiPrompt prompt = createPrompt(topic, "java");
                String responseJson = sendPrompt(prompt);
                String formattedResponse = jsonUtils.extractRelevantJson(responseJson);
                formattedResponse = jsonUtils.formatJsonResponse(formattedResponse);

                if (!isFirst) {
                    allResponses.append(",");
                }
                allResponses.append(formattedResponse);
                isFirst = false;
            } catch (Exception e) {
                logger.error("Error processing topic {}: {}", topic, e.getMessage());
            }
        }

        return allResponses.toString();
    }

    private Topics readTopicsFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:java.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, Topics.class);
        } catch (IOException e) {
            logger.error("Error reading topics file: {}", e.getMessage());
            throw new IOException("Failed to read topics file", e);
        }
    }

    private GeminiPrompt createPrompt(String topic, String subject) {
        GeminiPrompt prompt = new GeminiPrompt();

        // Create system instruction
        GeminiPrompt.SystemInstruction systemInstruction = new GeminiPrompt.SystemInstruction();
        GeminiPrompt.Part systemPart = new GeminiPrompt.Part();
        systemPart.setText("You are a technical interviewer. Provide interview questions in the exact JSON format specified.");
        systemInstruction.setParts(Collections.singletonList(systemPart));
        prompt.setSystemInstruction(systemInstruction);

        // Create content
        GeminiPrompt.Content content = new GeminiPrompt.Content();
        GeminiPrompt.Part contentPart = new GeminiPrompt.Part();
        String promptText = String.format(
                "Generate detailed interview questions for topic '%s' in '%s'. Format the response exactly as JSON: \n" +
                        "[\n  { \"Subject\": \"\", \"question\": \"\", \"idealAnswer\": \"\", " +
                        "\"category\": \"\", \"subCategory\": \"\", \"topic\": \"\", \"subTopic\": \"\", " +
                        "\"DifficultyLevel\": \"Easy/Medium/Hard\", \"Level\": \"1 to 5\" }\n]",
                topic, subject);
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
                    String errorMessage = "Unexpected response code: " + response.getCode();
                    logger.error(errorMessage);
                    throw new IOException(errorMessage);
                }
                return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            });
        } catch (IOException e) {
            logger.error("Error sending prompt to Gemini API: {}", e.getMessage());
            throw new IOException("Failed to send prompt to Gemini API", e);
        }
    }

    @Transactional
    public List<QuestionBank> processAndSaveTopics() throws IOException {
        String jsonResponse = processTopics();
        List<QuestionBank> savedQuestions = new ArrayList<>();

        try {
            if (!jsonResponse.trim().startsWith("[")) {
                jsonResponse = "[" + jsonResponse + "]";
            }

            logger.debug("Processing JSON response: {}", jsonResponse);

            List<QuestionBank> requests = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<QuestionBank>>() {}
            );

            for (QuestionBank request : requests) {
                try {
                    if (isValidRequest(request)) {
                        QuestionBank questionBank = convertToEntity(request);
                        QuestionBank savedQuestion = questionBankRepository.save(questionBank);
                        savedQuestions.add(savedQuestion);
                    } else {
                        logger.warn("Skipping invalid request: {}", request);
                    }
                } catch (Exception e) {
                    logger.error("Error processing request: {}. Error: {}", request, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JSON response: {}. Response was: {}", e.getMessage(), jsonResponse);
            throw new IOException("Failed to process JSON response: " + e.getMessage());
        }

        return savedQuestions;
    }


    private boolean isValidRequest( QuestionBank request) {
        return request != null
                && request.getQuestion() != null
                && !request.getQuestion().trim().isEmpty()
                && request.getSubject() != null
                && !request.getSubject().trim().isEmpty();
    }

    private QuestionBank convertToEntity(QuestionBank request) {
        if (request == null) {
            throw new IllegalArgumentException("QuestionBankRequest cannot be null");
        }

        QuestionBank questionBank = new QuestionBank();
        questionBank.setSubject(request.getSubject());
        questionBank.setQuestion(request.getQuestion());
        questionBank.setIdealAnswer(request.getIdealAnswer());
        questionBank.setCategory(request.getCategory());
        questionBank.setSubCategory(request.getSubCategory());
        questionBank.setTopic(request.getTopic());
        questionBank.setSubTopic(request.getSubTopic());
        questionBank.setDifficultyLevel(request.getDifficultyLevel());
        questionBank.setLevel(request.getLevel());
        questionBank.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return questionBank;
    }
}