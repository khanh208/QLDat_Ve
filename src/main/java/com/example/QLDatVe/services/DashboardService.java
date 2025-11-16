package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.DashboardStatsDTO;
import com.example.QLDatVe.dtos.TopRouteDTO;
import com.example.QLDatVe.repositories.BookingDetailRepository;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import com.example.QLDatVe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository; // Inject TripRepository

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. Lấy các chỉ số cơ bản
        stats.setTotalRevenue(bookingRepository.sumTotalRevenueConfirmed());
        stats.setTotalBookingsConfirmed(bookingRepository.countConfirmedBookings());
        stats.setTotalTicketsSold(bookingDetailRepository.countTotalTicketsSoldConfirmed());
        stats.setTotalUsers(userRepository.count()); // Đếm tổng số user

        // 2. Lấy Top 5 tuyến đường
        stats.setTopRoutes(tripRepository.findTopRoutes()); // Gọi hàm mới

        return stats;
    }
}