package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.DichVuService;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')") // Bảo vệ tất cả các endpoint trong controller này
public class CustomerController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private PhienSuDungService phienSuDungService;

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private PhienSuDungRepository phienSuDungRepository;

    /**
     * Helper method để lấy MaTK từ principal của người dùng đã được xác thực.
     * @param authentication Đối tượng xác thực chứa thông tin người dùng.
     * @return Mã tài khoản của người dùng.
     */
    private String getMaTKFromPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BadRequestException("Người dùng chưa được xác thực.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Giả sử username là TenTK, cần tra cứu MaTK từ TenTK
            TaiKhoan tk = taiKhoanRepository.findByTenTK(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với username: " + username));
            return tk.getMaTK();
        }
        // Nếu principal là một đối tượng khác hoặc bạn có cách lấy MaTK khác
        throw new BadRequestException("Không thể xác định Mã Tài Khoản từ thông tin xác thực.");
    }

    @GetMapping("/account/my-balance")
    public ResponseEntity<BigDecimal> getCurrentUserSoDu(Authentication authentication) {
        String maTKCurrentUser = getMaTKFromPrincipal(authentication);
        return ResponseEntity.ok(taiKhoanService.getSoDuTaiKhoan(maTKCurrentUser));
    }

    @GetMapping("/session/my-current-session/remaining-time")
    public void getCurrentSessionRemainingTime(Authentication authentication) {
        String maTKCurrentUser = getMaTKFromPrincipal(authentication);
        // Tìm phiên đang hoạt động của user hiện tại
        PhienSuDung activeSession = (PhienSuDung) phienSuDungRepository.findByTaiKhoan_MaTKAndThoiGianKetThucIsNull(maTKCurrentUser);
    }
    }