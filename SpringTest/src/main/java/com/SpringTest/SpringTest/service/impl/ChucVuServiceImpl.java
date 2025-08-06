package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.ChucVu;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.ChucVuRepository;
import com.SpringTest.SpringTest.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChucVuServiceImpl implements ChucVuService {

    @Autowired
    private ChucVuRepository chucVuRepository;

    @Override
    public List<ChucVu> getAllChucVu() {
        return chucVuRepository.findAll();
    }

    @Override
    public ChucVu findById(String maChucVu) {
        return chucVuRepository.findById(maChucVu)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ với mã: " + maChucVu));
    }

    // Implement các phương thức CRUD khác cho ChucVu nếu Admin có thể quản lý (thêm, sửa, xóa ChucVu)
    // Ví dụ:
    // @Override
    // public ChucVu saveChucVu(ChucVu chucVu) {
    //     return chucVuRepository.save(chucVu);
    // }
}