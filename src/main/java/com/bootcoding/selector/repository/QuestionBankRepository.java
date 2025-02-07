package com.bootcoding.selector.repository;

import com.bootcoding.selector.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, UUID> {
}