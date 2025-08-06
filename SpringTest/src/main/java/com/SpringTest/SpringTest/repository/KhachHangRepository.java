package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    Optional<KhachHang> findByTaiKhoan_MaTK(String maTK);
}