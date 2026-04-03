package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.UserUpdateRequest;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users") // <-- URL cho Admin
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // API Lấy tất cả người dùng
    // GET /api/admin/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // API Lấy 1 người dùng
    // GET /api/admin/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // API Cập nhật người dùng (Vô hiệu hóa / Đổi quyền)
    // PUT /api/admin/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody UserUpdateRequest request) {
        try {
            User updatedUser = userService.updateUser(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}