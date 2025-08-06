package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TaiKhoanService {
    TaiKhoanInfoResponse getTaiKhoanInfo(String maTK);
    BigDecimal getSoDuTaiKhoan(String maTK);
    TaiKhoanInfoResponse createTaiKhoanKhachHang(CreateTaiKhoanRequest request);
    TaiKhoanInfoResponse napTien(NapTienRequest request);

    TaiKhoanInfoResponse updateTaiKhoanKhachHang(String maTK, CreateTaiKhoanRequest request);

    long countAllActiveAccounts();

    TaiKhoan findEntityByMaTK(String maTK);

    Page<TaiKhoanInfoResponse> getAllKhachHangTaiKhoanPageable(Pageable pageable);

    long countNewAccountsSince(LocalDateTime startOfDay);

    long count();
    // Thêm các phương thức khác nếu cần

    void deleteById(String maTK);

    List<TaiKhoan> getAllTaiKhoan();
    TaiKhoan getTaiKhoanById(Integer maTK);
    TaiKhoan updateTaiKhoan(TaiKhoan taiKhoan);
    void deleteTaiKhoan(String maTK);

    TaiKhoan findByTenTK(String tenTK);
}