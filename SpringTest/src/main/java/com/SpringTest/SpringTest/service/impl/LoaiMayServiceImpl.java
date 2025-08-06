package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.LoaiMay;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.LoaiMayRepository;
import com.SpringTest.SpringTest.service.LoaiMayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoaiMayServiceImpl implements LoaiMayService {

    @Autowired
    private LoaiMayRepository loaiMayRepository;

    @Override
    public List<LoaiMay> getAllLoaiMay() {
        return loaiMayRepository.findAll();
    }

    public LoaiMay getLoaiMayById(String maLoaiMay) {
        return loaiMayRepository.findById(maLoaiMay)
                .orElseThrow(() -> new ResourceNotFoundException("Loại máy không tồn tại: " + maLoaiMay));
    }

    // Implement các phương thức CRUD khác cho LoaiMay nếu Admin có thể quản lý
    // Ví dụ:
    // @Override
    // public LoaiMay saveLoaiMay(LoaiMay loaiMay) {
    //     return loaiMayRepository.save(loaiMay);
    // }
}