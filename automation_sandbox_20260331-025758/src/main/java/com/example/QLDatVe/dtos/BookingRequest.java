package com.example.QLDatVe.dtos;

import lombok.Data;
import java.util.List; // Phải import List

@Data
public class BookingRequest {
    
    private int tripId;
    
    // HÃY CHẮC CHẮN RẰNG ĐÂY LÀ LIST (SỐ NHIỀU)
    private List<String> seatNumbers;
    private String paymentMethod; 
}