package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.CaLamViec;
import com.SpringTest.SpringTest.entity.NhanVien;
import com.SpringTest.SpringTest.entity.NhanVienCaLamViec;
import com.SpringTest.SpringTest.entity.NhanVienCaLamViecId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NhanVienCaLamViecRepository extends JpaRepository<NhanVienCaLamViec, NhanVienCaLamViecId> {
    List<NhanVienCaLamViec> findByNhanVien(NhanVien nhanVien);
    List<NhanVienCaLamViec> findById_MaNV(String maNV);
    List<NhanVienCaLamViec> findByCaLamViec(CaLamViec caLamViec);
    List<NhanVienCaLamViec> findById_MaCaLamViec(String maCaLamViec);
    @Query(value = "SELECT fn_TinhTienCongMotCaLamViec(:maNV, :maCaLamViec)", nativeQuery = true)
    BigDecimal calculateShiftSalary(@Param("maNV") String maNV, @Param("maCaLamViec") String maCaLamViec);
    boolean existsById_MaNV(String maNV);
}