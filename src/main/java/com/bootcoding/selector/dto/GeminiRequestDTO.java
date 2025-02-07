package com.bootcoding.selector.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeminiRequestDTO {
    private SystemInstruction system_instruction;
    private List<Content> contents;

    @Data
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }
}
