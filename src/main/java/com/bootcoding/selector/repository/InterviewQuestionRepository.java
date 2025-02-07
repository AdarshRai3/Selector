package com.bootcoding.selector.repository;

import com.bootcoding.selector.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {
}