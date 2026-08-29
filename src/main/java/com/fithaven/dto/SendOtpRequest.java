package com.fithaven.dto;

import jakarta.validation.constraints.NotBlank;

public class SendOtpRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String mode; // "signin" or "signup"

    public SendOtpRequest() {}

    public SendOtpRequest(String phone) {
        this.phone = phone;
    }

    public SendOtpRequest(String phone, String mode) {
        this.phone = phone;
        this.mode = mode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
