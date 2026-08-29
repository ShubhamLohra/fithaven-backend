package com.fithaven.repository;

import com.fithaven.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByPhoneOrderByTimestampDesc(String phone);
}
