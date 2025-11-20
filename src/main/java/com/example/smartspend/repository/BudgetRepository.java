package com.example.smartspend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.Category;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(Long userId, Category category, Integer month, Integer year);
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    boolean existsByUserIdAndCategoryAndMonthAndYear(Long userId, Category category, Integer month, Integer year);
}