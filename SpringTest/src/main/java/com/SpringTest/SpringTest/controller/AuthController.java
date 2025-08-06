package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.dto.request.LoginRequest;
import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import com.SpringTest.SpringTest.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<PhienSuDungInfoResponse> login(@RequestBody LoginRequest loginRequest) {
        PhienSuDungInfoResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/{maPhien}")
    // @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'MANAGER')") // Hoặc chỉ customer
    public ResponseEntity<String> logout(@PathVariable int maPhien) {
        authService.logout(maPhien);
        return ResponseEntity.ok("Đăng xuất thành công phiên " + maPhien);
    }
}