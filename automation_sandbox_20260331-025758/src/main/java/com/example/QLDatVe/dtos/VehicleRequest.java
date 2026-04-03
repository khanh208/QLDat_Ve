// Đặt trong package 'dtos'
package com.example.QLDatVe.dtos;

import lombok.Data;

@Data
public class VehicleRequest {
    // Dùng cho cả Tạo mới và Cập nhật
    private String licensePlate; // Biển số xe
    private String vehicleType;  // Loại xe (VD: Giường nằm 40 chỗ)
    private int totalSeats;      // Tổng số ghế
}