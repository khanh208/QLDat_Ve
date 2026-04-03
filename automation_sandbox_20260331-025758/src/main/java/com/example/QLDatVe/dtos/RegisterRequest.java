package com.example.QLDatVe.dtos;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName; // (Tùy chọn)
    private String phone; // (Ví dụ: "USER" hoặc "ADMIN")
}