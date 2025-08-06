package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.MayTinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface MayTinhService {
    List<MayTinh> getAllMayTinh();
    List<MayTinh> getAllMayTinhList();
    MayTinh getMayTinhById(String maMay);
    MayTinh updateTrangThaiMay(String maMay, String trangThaiMoi);
    MayTinh addMayTinh(MayTinh mayTinh); // Cho Manager
    void deleteMayTinh(String maMay); // Cho Manager
    MayTinh updateMayTinh(String maMay, MayTinh mayTinhDetails); // Cho Manager

    long countAllMayTinh();

    long countMayTinhByTrangThai(String đangSửDụng);

    Page<MayTinh> findPaginated(PageRequest of, String searchKeyword);

    long count();

    List<MayTinh> getAvailableComputers();
}