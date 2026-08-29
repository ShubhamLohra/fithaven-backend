package com.fithaven.service;

import com.fithaven.model.OtpToken;
import com.fithaven.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${fast2sms.api.key:}")
    private String fast2smsApiKey;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    public String generateAndSaveOtp(String phone) {
        String code = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpToken otpToken = new OtpToken(phone, code, expiresAt);
        otpTokenRepository.save(otpToken);

        // Attempt dispatching real SMS via Fast2SMS
        sendFast2Sms(phone, code);

        return code;
    }

    private void sendFast2Sms(String phone, String code) {
        if (fast2smsApiKey == null || fast2smsApiKey.trim().isEmpty() || fast2smsApiKey.contains("YOUR_FAST2SMS_API_KEY") || fast2smsApiKey.contains("DEV_MODE")) {
            System.out.println("Fast2SMS API key set to Dev Mode. Skipping paid SMS API call to save credits.");
            return;
        }

        try {
            String cleanPhone = phone.replaceAll("\\D", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }

            HttpClient client = HttpClient.newHttpClient();

            // Try Quick SMS route (route 'q') which bypasses DLT website verification
            String quickSmsPayload = String.format(
                "{\"route\":\"q\",\"message\":\"Your FitHaven verification code is %s\",\"language\":\"english\",\"flash\":\"0\",\"numbers\":\"%s\"}",
                code, cleanPhone
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                    .header("authorization", fast2smsApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(quickSmsPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Fast2SMS Quick SMS Dispatch Result: " + response.body());

            // If Quick SMS reports status error, attempt fallback to OTP route
            if (response.body().contains("status_code\":99") || response.body().contains("false")) {
                String otpPayload = String.format(
                    "{\"route\":\"otp\",\"variables_values\":\"%s\",\"numbers\":\"%s\"}",
                    code, cleanPhone
                );
                HttpRequest fallbackRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                        .header("authorization", fast2smsApiKey.trim())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(otpPayload))
                        .build();
                HttpResponse<String> fallbackResponse = client.send(fallbackRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("Fast2SMS OTP Route Dispatch Result: " + fallbackResponse.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to dispatch real SMS via Fast2SMS: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String phone, String code) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findTopByPhoneAndVerifiedFalseOrderByExpiresAtDesc(phone);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        OtpToken token = tokenOpt.get();

        if (token.isExpired()) {
            return false;
        }

        if (token.getCode().equals(code)) {
            token.setVerified(true);
            otpTokenRepository.save(token);
            return true;
        }

        return false;
    }
}
