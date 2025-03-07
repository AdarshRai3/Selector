package com.bootcoding.selector.controller;

import com.bootcoding.selector.entity.QuestionBank;
import com.bootcoding.selector.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {
    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<QuestionBank>> generateAndSaveContent() throws IOException {
        List<QuestionBank> savedQuestions = geminiService.processAndSaveTopics();
        return ResponseEntity.ok(savedQuestions);
    }
}