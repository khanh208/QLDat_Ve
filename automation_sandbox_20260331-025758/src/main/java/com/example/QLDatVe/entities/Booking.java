package com.example.QLDatVe.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    // ✅ EAGER fetch để tránh lazy loading error
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "vehicle"}) // Tránh vòng lặp JSON
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "bookings", "authorities"}) // Ẩn thông tin nhạy cảm
    private User user;

    @Column(name = "booking_date")
    private LocalDateTime bookingTime;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", length = 50)
    private String status;

    // === MoMo tracking ===
    @Column(name = "momo_order_id", length = 100, unique = true)
    private String momoOrderId;

    @Column(name = "momo_request_id", length = 100)
    private String momoRequestId;

    // ✅ EAGER fetch cho booking details
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("booking") // Tránh vòng lặp JSON
    private List<BookingDetail> bookingDetails;

    @PrePersist
    protected void onCreate() {
        bookingTime = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}