package com.example.QLDatVe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "role", nullable = false, length = 20)
    private String role;
    
    // --- CÁC TRƯỜNG MỚI ĐỂ XÁC THỰC BẰNG MÃ OTP ---
    
    @Column(name = "enabled")
    private boolean enabled = false; // Mặc định là 'false'

    @Column(name = "verification_token")
    private String verificationCode; // Mã 5 số để kích hoạt

    @Column(name = "password_reset_token")
    private String passwordResetCode; // Mã 5 số để reset pass

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate; // Hạn dùng của mã
    
    // --- HẾT TRƯỜNG MỚI ---

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;

    
    // --- Các phương thức bắt buộc của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; 
    }

    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Trả về trạng thái 'enabled' (true/false)
        return this.enabled; 
    }
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) // User không cần EAGER
    @JsonIgnore // Không serialize bookings khi trả về user
    private List<Booking> bookings;
}