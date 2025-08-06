package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.PhienSuDung;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhienSuDungService {
    PhienSuDung batDauPhienSuDung(String maMay, String maTK);
    PhienSuDung ketThucPhienSuDung(Integer maPhien);
    List<PhienSuDung> getPhienSuDungByTaiKhoan(String maTK);
    long tinhThoiGianConLaiPhut(Integer maPhien);
    long countByTrangThai(String trangThai);
    List<PhienSuDung> findAll();
    PhienSuDung findById(Integer id);
    PhienSuDung save(PhienSuDung phienSuDung);
    void deleteById(Integer id);
    long countByThoiGianKetThucIsNull();
    Page<PhienSuDung> findAll(Pageable pageable);
    Page<PhienSuDung> getAllPhienSuDung(Pageable pageable);
    PhienSuDung createPhienSuDung(String maMay, String maTK);
    void endPhienSuDung(String maPhien);
    void deletePhienSuDung(Integer maPhien);
}