package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.*;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService; 
    private final AuthenticationManager authenticationManager;

    /**
     * Hàm nội bộ để tạo mã OTP 5 số
     */
    private String generateOtp() {
        return String.format("%05d", new Random().nextInt(100000));
    }

    /**
     * 1. HÀM ĐĂNG KÝ (Đã thêm 'phone')
     */
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); 
        user.setRole("USER"); // Ép quyền là USER
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone()); // <-- **DÒNG BẠN BỊ THIẾU NẰM Ở ĐÂY**
        
        user.setEnabled(false); // Tắt tài khoản
        
        String code = generateOtp();
        user.setVerificationCode(code);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5)); // Hạn 5 phút

        User savedUser = userRepository.save(user);

        // Gửi email chứa MÃ OTP
        String emailBody = "Chào bạn " + user.getUsername() + ",\n\n"
                         + "Mã kích hoạt tài khoản của bạn là: " + code + "\n\n"
                         + "Mã này sẽ hết hạn sau 5 phút.";
        emailService.sendEmail(user.getEmail(), "Kích hoạt tài khoản QLDatVe", emailBody);

        return savedUser;
    }

    /**
     * 2. HÀM ĐĂNG NHẬP
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực (Giữ nguyên)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // 2. Nếu xác thực thành công, tìm user (bằng username)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // 3. Tạo token
        String token = jwtService.generateToken(user);
        
        // 4. Trả về token
       return new AuthResponse(
            token, 
            "Đăng nhập thành công",
            user.getUserId(),
            user.getUsername(),
            user.getRole()
        );
    }

    /**
     * 3. HÀM XÁC THỰC
     */
    public String verifyAccount(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.isEnabled()) {
            return "Tài khoản này đã được kích hoạt trước đó.";
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Mã kích hoạt không chính xác.");
        }

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã kích hoạt đã hết hạn.");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return "Tài khoản đã được kích hoạt thành công!";
    }

    /**
     * 4. HÀM QUÊN MẬT KHẨU
     */
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            String code = generateOtp();
            user.setPasswordResetCode(code);
            user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5)); // Hạn 5 phút
            userRepository.save(user);

            String emailBody = "Chào bạn " + user.getUsername() + ",\n\n"
                             + "Mã lấy lại mật khẩu của bạn là: " + code + "\n\n"
                             + "Mã này sẽ hết hạn sau 5 phút.";
            emailService.sendEmail(user.getEmail(), "Yêu cầu lấy lại mật khẩu", emailBody);
        }
        
        return "Nếu email tồn tại, một mã OTP đã được gửi.";
    }

    /**
     * 5. HÀM ĐẶT LẠI MẬT KHẨU
     */
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getPasswordResetCode() == null || !user.getPasswordResetCode().equals(request.getCode())) {
            throw new RuntimeException("Mã reset không chính xác.");
        }

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã reset đã hết hạn.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword())); 
        user.setPasswordResetCode(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return "Mật khẩu đã được thay đổi thành công!";
    }
}