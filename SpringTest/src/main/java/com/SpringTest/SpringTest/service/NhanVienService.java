package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NhanVienService {
    Page<NhanVien> getAllNhanVien(Pageable pageable);
    List<NhanVien> getAllNhanVienList(); // Nếu cần
    NhanVien getNhanVienById(String maNV);
    NhanVien saveNhanVien(NhanVien nhanVien); // Hoặc nhận NhanVienFormDTO
    NhanVien updateNhanVien(String maNV, NhanVien nhanVienDetails); // Hoặc nhận DTO
    void deleteNhanVien(String maNV); // API DELETE sẽ gọi hàm này
}