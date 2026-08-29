package com.fithaven.controller;

import com.fithaven.model.Meal;
import com.fithaven.repository.MealRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealRepository mealRepository;

    public MealController(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    @GetMapping
    public ResponseEntity<List<Meal>> getMeals(@RequestParam String phone) {
        List<Meal> meals = mealRepository.findByPhoneOrderByTimestampDesc(phone);
        return ResponseEntity.ok(meals);
    }

    @PostMapping
    public ResponseEntity<Meal> addMeal(@RequestBody Meal meal) {
        if (meal.getPhone() == null || meal.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Meal savedMeal = mealRepository.save(meal);
        return ResponseEntity.ok(savedMeal);
    }
}
