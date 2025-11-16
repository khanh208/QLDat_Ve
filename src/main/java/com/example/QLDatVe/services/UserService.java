package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.ChangePasswordRequest;
import com.example.QLDatVe.dtos.ProfileUpdateRequest;
import com.example.QLDatVe.dtos.UserUpdateRequest;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Lấy tất cả người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Lấy 1 người dùng theo ID
    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    // 3. Cập nhật (vô hiệu hóa / đổi quyền)
    public User updateUser(int id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + id));

        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());

        return userRepository.save(user);
    }
    public User updateUserProfile(User user, ProfileUpdateRequest request) {
        // User đã được lấy từ Security Context
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return userRepository.save(user);
    }
    public void changePassword(User user, ChangePasswordRequest request) {
        // 1. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác.");
        }
        // 2. Kiểm tra mật khẩu mới có rỗng không
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
             throw new RuntimeException("Mật khẩu mới không được để trống.");
        }
        // 3. Mã hóa và lưu mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}