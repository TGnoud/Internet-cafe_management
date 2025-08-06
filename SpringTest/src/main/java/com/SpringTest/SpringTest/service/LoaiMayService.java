package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.LoaiMay;

import java.util.List;

public interface LoaiMayService {
    List<LoaiMay> getAllLoaiMay();
    // Thêm các hàm CRUD khác nếu quản trị viên có thể quản lý LoaiMay
}