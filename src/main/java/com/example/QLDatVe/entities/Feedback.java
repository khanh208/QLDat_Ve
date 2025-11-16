// src/main/java/com/example/QLDatVe/entities/Feedback.java
package com.example.QLDatVe.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    // ⭐ FIX: Thêm JsonIgnoreProperties để tránh infinite loop
    @ManyToOne(fetch = FetchType.EAGER) // ⭐ Đổi sang EAGER để load user info
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "passwordHash", "bookings", "feedbacks", "authorities", 
                           "enabled", "accountNonExpired", "accountNonLocked", 
                           "credentialsNonExpired", "verificationCode", "passwordResetCode", 
                           "tokenExpiryDate"}) // ⭐ Chỉ serialize thông tin cần thiết
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // ⭐ Đổi sang EAGER để load trip info
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "feedbacks", "bookingDetails"}) // ⭐ Tránh infinite loop
    private Trip trip;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "feedback_time")
    private LocalDateTime feedbackTime;

    @PrePersist
    protected void onCreate() {
        feedbackTime = LocalDateTime.now();
    }
}