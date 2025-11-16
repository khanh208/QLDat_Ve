package com.example.QLDatVe.dtos;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String role; // Ví dụ: "USER" hoặc "ADMIN"
    private boolean enabled; // true (kích hoạt) hoặc false (vô hiệu hóa)
}