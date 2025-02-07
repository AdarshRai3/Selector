package com.bootcoding.selector.entity;

import jakarta.persistence.*;
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
@Table(name = "interview_question", schema = "public")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "question_bank_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID questionBankId;  // Stores the UUID from QuestionBank but does not establish a join.

    @Column(name = "subject", nullable = false, columnDefinition = "TEXT")
    private String subject;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "ideal_answer", nullable = false, columnDefinition = "TEXT")
    private String idealAnswer;

    @Column(name = "category", nullable = false, columnDefinition = "TEXT")
    private String category;

    @Column(name = "sub_category", nullable = false, columnDefinition = "TEXT")
    private String subCategory;

    @Column(name = "topic", nullable = false, columnDefinition = "TEXT")
    private String topic;

    @Column(name = "sub_topic", nullable = false, columnDefinition = "TEXT")
    private String subTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, columnDefinition = "VARCHAR")
    private QuestionBank.DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "level", nullable = false)
    private QuestionBank.Level level;

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
}
