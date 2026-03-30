package com.example.QLDatVe.repositories;

import com.example.QLDatVe.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Dùng cho Login, Quên mật khẩu, Kích hoạt
    Optional<User> findByEmail(String email);
    
    // Dùng cho Login (nếu bạn login bằng username)
    Optional<User> findByUsername(String username);
}