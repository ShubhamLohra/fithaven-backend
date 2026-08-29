package com.fithaven.controller;

import com.fithaven.dto.AuthResponse;
import com.fithaven.dto.SendOtpRequest;
import com.fithaven.dto.VerifyOtpRequest;
import com.fithaven.model.User;
import com.fithaven.repository.UserRepository;
import com.fithaven.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;

    public AuthController(OtpService otpService, UserRepository userRepository) {
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String phone = request.getPhone().trim();
        String mode = request.getMode() != null ? request.getMode().trim().toLowerCase() : "signin";

        Optional<User> userOpt = userRepository.findByPhone(phone);
        boolean userExists = userOpt.isPresent();
        boolean profileCompleted = userExists && userOpt.get().getAge() != null;

        if ("signin".equalsIgnoreCase(mode) && !userExists) {
            return ResponseEntity.status(404).body(new AuthResponse(
                    false,
                    "No account found with this phone number. Please use 'Join US' to create your account."
            ));
        }

        if ("signup".equalsIgnoreCase(mode) && profileCompleted) {
            return ResponseEntity.badRequest().body(new AuthResponse(
                    false,
                    "An account with this phone number already exists. Please Sign In."
            ));
        }

        String generatedOtp = otpService.generateAndSaveOtp(phone);

        return ResponseEntity.ok(new AuthResponse(
                true,
                "OTP verification code generated successfully.",
                generatedOtp
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String phone = request.getPhone().trim();
        String code = request.getOtp().trim();

        boolean isValid = otpService.verifyOtp(phone, code);

        if (!isValid) {
            return ResponseEntity.badRequest().body(new AuthResponse(
                    false,
                    "Invalid or expired OTP code. Please try again."
            ));
        }

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> userRepository.save(new User(phone)));

        return ResponseEntity.ok(new AuthResponse(
                true,
                "Verification successful.",
                user
        ));
    }
}
