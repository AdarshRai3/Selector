package com.bootcoding.selector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GeminiPrompt {
    @JsonProperty("system_instruction")
    private SystemInstruction systemInstruction;

    private List<Content> contents;

    // Getters and Setters
    public SystemInstruction getSystemInstruction() {
        return systemInstruction;
    }

    public void setSystemInstruction(SystemInstruction systemInstruction) {
        this.systemInstruction = systemInstruction;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    // Nested static classes
    public static class SystemInstruction {
        private List<Part> parts;

        // Getter
        public List<Part> getParts() {
            return parts;
        }

        // Setter
        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Content {
        private List<Part> parts;

        // Getter
        public List<Part> getParts() {
            return parts;
        }

        // Setter
        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        // Getter
        public String getText() {
            return text;
        }

        // Setter
        public void setText(String text) {
            this.text = text;
        }
    }

    // No-args constructor
    public GeminiPrompt() {}
}