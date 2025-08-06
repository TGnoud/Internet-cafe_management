package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.ChiTietHoaDonDV;
import com.SpringTest.SpringTest.entity.ChiTietHoaDonDVId;
import com.SpringTest.SpringTest.entity.DichVu;
import com.SpringTest.SpringTest.entity.HoaDonDV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietHoaDonDVRepository extends JpaRepository<ChiTietHoaDonDV, ChiTietHoaDonDVId> {
    List<ChiTietHoaDonDV> findByHoaDonDV(HoaDonDV hoaDonDV);
    List<ChiTietHoaDonDV> findById_MaHD(String maHD); // Tìm theo MaHD từ ID
    List<ChiTietHoaDonDV> findByDichVu(DichVu dichVu);
    List<ChiTietHoaDonDV> findById_MaDV(String maDV);   // Tìm theo MaDV từ ID
    boolean existsByDichVu_MaDV(String maDV);
}