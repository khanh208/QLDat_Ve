package com.example.QLDatVe.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bookingdetails") 
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id") 
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // HÃY CHẮC CHẮN BẠN CÓ TRƯỜNG NÀY
    // VÀ @Column KHỚP VỚI CSDL
    @Column(name = "seat_number", length = 10) 
    private String seatNumber;
    
    // (Không có trường "price")
}