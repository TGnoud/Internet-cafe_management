package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.ChucVu;

import java.util.List;

public interface ChucVuService {
    List<ChucVu> getAllChucVu();
    ChucVu findById(String maChucVu);
    // Thêm các hàm CRUD khác nếu quản trị viên có thể quản lý ChucVu
}