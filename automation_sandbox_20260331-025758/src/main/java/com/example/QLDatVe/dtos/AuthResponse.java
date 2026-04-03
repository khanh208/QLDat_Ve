package com.example.QLDatVe.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor 
public class AuthResponse {
    private String token;
    private String message;
    
    // --- THÊM 3 TRƯỜNG NÀY ---
    private Integer userId;
    private String username;
    private String role;
}