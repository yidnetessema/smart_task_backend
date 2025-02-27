package com.inkomoko.smarttask.security.controller;

import com.inkomoko.smarttask.security.dto.*;
import com.inkomoko.smarttask.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationController {
    private final AuthenticationService service;


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register (@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.authenticate(request,httpServletRequest));
    }

    @GetMapping("/check/email/{email}/phone-number/{phoneNumber}")
    public ResponseEntity<?> checkEmailAndPhoneNumber(@PathVariable(name = "email") String email, @PathVariable(name="phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(service.checkEmailAndPhoneNumber(email, phoneNumber));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, Object>> passwordResetRequest(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.passwordChangeRequest(request.getEmail()));
    }

    @PostMapping("/password-reset:verify")
    public ResponseEntity<Map<String, Object>> passwordResetRequestVerify(@RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(service.verifyPasswordChange(request));
    }

    @PostMapping("/password-reset:confirm")
    public ResponseEntity<Map<String, Object>> passwordResetRequestConfirm(@RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(service.resetPassword(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody JSONObject request) {
        return ResponseEntity.ok(service.verifyEmail(request.get("email").toString()));
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<Map<String, Object>> verifyEmailOtp(@RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(service.verifyEmailOtp(request));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<Map<String, Object>> verifyPhone(@RequestBody JSONObject jsonObject) {
        return ResponseEntity.ok(service.verifyPhone(jsonObject.get("phone").toString()));
    }

}