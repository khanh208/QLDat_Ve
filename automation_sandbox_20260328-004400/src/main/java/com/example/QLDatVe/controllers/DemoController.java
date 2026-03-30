// Đặt trong package 'controllers'
package com.example.QLDatVe.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo") // Một đường dẫn được bảo vệ
public class DemoController {

    // API này yêu cầu phải xác thực (phải có token)
    // GET: http://localhost:8080/api/v1/demo
    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Xin chào, bạn đã vượt qua được bảo mật!");
    }
}