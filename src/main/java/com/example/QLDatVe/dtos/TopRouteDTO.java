package com.example.QLDatVe.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Tạo constructor cho dễ
public class TopRouteDTO {
    private String routeName; // Tên tuyến (VD: "TP.HCM - Đà Lạt")
    private long ticketCount; // Số vé bán được
}