package com.fithaven.controller;

import com.fithaven.model.User;
import com.fithaven.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam String phone) {
        Optional<User> user = userRepository.findByPhone(phone);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<User> updateProfile(@RequestBody User userProfile) {
        if (userProfile.getPhone() == null || userProfile.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User existingUser = userRepository.findByPhone(userProfile.getPhone())
                .orElseGet(() -> new User(userProfile.getPhone()));

        existingUser.setName(userProfile.getName());
        existingUser.setAge(userProfile.getAge());
        existingUser.setGender(userProfile.getGender());
        existingUser.setHeight(userProfile.getHeight());
        existingUser.setWeight(userProfile.getWeight());
        existingUser.setActivityLevel(userProfile.getActivityLevel());
        existingUser.setGoal(userProfile.getGoal());
        existingUser.setBmi(userProfile.getBmi());
        existingUser.setDailyGoal(userProfile.getDailyGoal());
        existingUser.setPetType(userProfile.getPetType());
        existingUser.setPetName(userProfile.getPetName());

        User savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }
}
