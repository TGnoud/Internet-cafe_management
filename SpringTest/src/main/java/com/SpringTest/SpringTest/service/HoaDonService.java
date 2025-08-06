package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.dto.HoaDonDTO;
import com.SpringTest.SpringTest.dto.request.CreateHoaDonRequest;
import com.SpringTest.SpringTest.entity.HoaDonDV;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface HoaDonService {
    HoaDonDTO createHoaDon(CreateHoaDonRequest request);
    HoaDonDTO getHoaDonById(String maHD);
    List<HoaDonDTO> getHoaDonByMaTK(String maTK);
    List<HoaDonDTO> getHoaDonByMaNV(String maNV);
    Page<HoaDonDTO> getAllHoaDon(Pageable pageable);

    HoaDonDV createHoaDonDV(CreateHoaDonRequest request);
    // Thêm các phương thức tìm kiếm, thống kê khác nếu cần
    BigDecimal calculateBillTotal(String maHD, boolean applyDiscount);
    HoaDonDTO updateHoaDon(String maHD, CreateHoaDonRequest request);
}