package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.*;
import com.example.QLDatVe.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException; // Import AuthenticationException
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * API Đăng Ký (Gửi mã OTP)
     * POST: /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("Đăng ký thành công. Vui lòng kiểm tra email để lấy mã kích hoạt.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API Đăng Nhập (Đã sửa lỗi và try-catch)
     * POST: /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            // AuthService.login() now returns AuthResponse with user details
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            // Catches "Bad Credentials" or "User is disabled"
            // Return 401 Unauthorized with the error message
            AuthResponse errorResponse = new AuthResponse(null, e.getMessage(), null, null, null); // Token is null
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (RuntimeException e) {
            // Catches other errors like "User not found"
             AuthResponse errorResponse = new AuthResponse(null, e.getMessage(), null, null, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    /**
     * API KÍCH HOẠT TÀI KHOẢN (Bằng mã OTP)
     * POST: /api/auth/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestBody VerifyRequest request) {
        try {
            String message = authService.verifyAccount(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API QUÊN MẬT KHẨU (Gửi mã OTP)
     * POST: /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody EmailRequest request) {
        String message = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(message);
    }

    /**
     * API ĐẶT LẠI MẬT KHẨU (Bằng mã OTP)
     * POST: /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String message = authService.resetPassword(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}