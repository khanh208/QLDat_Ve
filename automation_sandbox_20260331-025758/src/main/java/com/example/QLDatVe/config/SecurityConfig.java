package com.example.QLDatVe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API
            .cors(c -> c.configurationSource(corsConfigurationSource())) // Áp dụng cấu hình CORS
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng session
            
            .authorizeHttpRequests(auth -> auth
                
                // --- 1. LUỒNG PUBLIC (Không cần Token) ---
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Cho phép CORS preflight
                .requestMatchers("/api/auth/**").permitAll() // Tất cả API Xác thực (Login, Register, Verify...)
                .requestMatchers("/api/payment/momo-ipn").permitAll() // MoMo IPN Webhook
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                
                // Cho phép xem (GET) tất cả thông tin liên quan đến Chuyến đi
                .requestMatchers(HttpMethod.GET, "/api/trips/**").permitAll() 
                // (Bao gồm: /api/trips, /api/trips/{id}, /api/trips/search, 
                // /api/trips/{id}/booked-seats, /api/trips/{id}/feedback)
                
                
                // --- 2. LUỒNG CẦN ĐĂNG NHẬP (USER hoặc ADMIN) ---
                .requestMatchers("/api/profile/**").authenticated() // Xem/Sửa thông tin cá nhân
                .requestMatchers("/api/bookings/**").authenticated() // Đặt vé, Hủy vé, Xem lịch sử
                .requestMatchers(HttpMethod.POST, "/api/feedback").authenticated() // Gửi đánh giá
                .requestMatchers("/api/payment/momo/checkout/**").authenticated() // Tạo thanh toán MoMo
                

                // --- 3. LUỒNG ADMIN (Chỉ ADMIN) ---
                // Quy tắc này bao gồm TẤT CẢ các đường dẫn /api/admin/
                // (Bao gồm /admin/users, /admin/trips, /admin/routes, /admin/vehicles, 
                // /admin/bookings, /admin/dashboard, /admin/feedback)
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                
                
                // --- 4. TẤT CẢ CÁC API CÒN LẠI ---
                .anyRequest().authenticated() // Tất cả các request khác đều phải đăng nhập
            )
            
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS (Giữ nguyên của bạn)
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Cache-Control"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}