package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.ChangePasswordRequest;
import com.example.QLDatVe.dtos.ProfileUpdateRequest;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile") // <-- URL mới
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    // Hàm helper để lấy User đang đăng nhập
    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    /**
     * API Lấy thông tin profile của tôi
     * GET /api/profile
     */
    @GetMapping
    public ResponseEntity<User> getMyProfile() {
        // Trả về đối tượng User đầy đủ (đã được bảo vệ)
        return ResponseEntity.ok(getLoggedInUser());
    }

    /**
     * API Cập nhật thông tin profile (Tên, SĐT)
     * PUT /api/profile
     */
    @PutMapping
    public ResponseEntity<User> updateMyProfile(@RequestBody ProfileUpdateRequest request) {
        User updatedUser = userService.updateUserProfile(getLoggedInUser(), request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * API Thay đổi mật khẩu
     * PUT /api/profile/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changeMyPassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(getLoggedInUser(), request);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}