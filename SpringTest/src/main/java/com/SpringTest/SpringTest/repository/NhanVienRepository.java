package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {
    @Query(value = "SELECT fn_TinhTienCongMotCaLamViec(:maNV, :maCaLamViec)", nativeQuery = true)
    BigDecimal calculateShiftSalary(@Param("maNV") String maNV, @Param("maCaLamViec") String maCaLamViec);
}