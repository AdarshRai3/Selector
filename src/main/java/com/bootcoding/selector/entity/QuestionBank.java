package com.bootcoding.selector.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_bank", schema = "public")
public class QuestionBank {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @NotBlank(message = "Subject cannot be empty")
    @Column(name = "subject", nullable = false, columnDefinition = "TEXT")
    private String subject;

    @NotBlank(message = "Question cannot be empty")
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @NotBlank(message = "Ideal answer cannot be empty")
    @Column(name = "ideal_answer", nullable = false, columnDefinition = "TEXT")
    private String idealAnswer;

    @NotBlank(message = "Category cannot be empty")
    @Column(name = "category", nullable = false, columnDefinition = "TEXT")
    private String category;

    @NotBlank(message = "Sub-category cannot be empty")
    @Column(name = "sub_category", nullable = false, columnDefinition = "TEXT")
    private String subCategory;

    @NotBlank(message = "Topic cannot be empty")
    @Column(name = "topic", nullable = false, columnDefinition = "TEXT")
    private String topic;

    @NotBlank(message = "Sub-topic cannot be empty")
    @Column(name = "sub_topic", nullable = false, columnDefinition = "TEXT")
    private String subTopic;

    @NotNull(message = "Difficulty level cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, columnDefinition = "VARCHAR")
    private DifficultyLevel difficultyLevel;

    @NotNull(message = "Level cannot be null")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "level", nullable = false)
    private Level level;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    public enum Level {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

        private final int value;

        Level(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Level fromValue(int value) {
            for (Level level : values()) {
                if (level.value == value) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Invalid level: " + value);
        }
    }
}
