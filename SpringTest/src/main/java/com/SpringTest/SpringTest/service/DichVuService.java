package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;

import java.util.List;

public interface DichVuService {
    // Cho Khách hàng
    void customerOrderService(OrderServiceRequest request, String maTKCurrentUser); // maTKCurrentUser để xác thực

    // Cho Nhân viên/Quản lý
    DichVuDTO addDichVu(DichVuDTO dichVuDTO);
    DichVuDTO updateDichVu(String maDV, DichVuDTO dichVuDTO);
    DichVuDTO getDichVuByMaDV(String maDV);
    List<DichVuDTO> getAllDichVu();
    void deleteDichVu(String maDV); // Cân nhắc soft delete
    List<DichVuDTO> findAvailableDichVu();
    long count();
}