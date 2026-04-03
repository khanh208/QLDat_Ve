package com.example.QLDatVe.dtos;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String code;
}