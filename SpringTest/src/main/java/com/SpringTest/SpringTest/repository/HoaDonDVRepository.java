package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.HoaDonDV;
import com.SpringTest.SpringTest.entity.NhanVien;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface HoaDonDVRepository extends JpaRepository<HoaDonDV, String> {
    List<HoaDonDV> findByTaiKhoan(TaiKhoan taiKhoan);
    List<HoaDonDV> findByNhanVien(NhanVien nhanVien);
    List<HoaDonDV> findByThoiDiemThanhToanBetween(LocalDateTime startDate, LocalDateTime endDate);
    // Cách gọi SQL Function TongTienDichVu
    @Query(value = "SELECT TongTienDichVu(:maHoaDon)", nativeQuery = true)
    BigDecimal getTongTienDichVu(@Param("maHoaDon") Integer maHoaDon);

    // Bạn cũng có thể sử dụng function trong một câu SELECT phức tạp hơn
    @Query(value = "SELECT hd.*, TongTienDichVu(hd.MaHoaDonDV) AS tongChiPhiDichVu FROM HoaDonDV hd WHERE hd.MaHoaDonDV = :maHoaDon", nativeQuery = true)
    Object getHoaDonWithTongTienDichVu(@Param("maHoaDon") Integer maHoaDon);// Kiểu trả về có thể là Object[] hoặc một DTO projection
    @Query(value = "SELECT TinhTongChiPhiHoaDonDV(:maHD, :apDungUuDai)", nativeQuery = true)
    BigDecimal calculateServiceBillCost(@Param("maHD") String maHD, @Param("apDungUuDai") boolean apDungUuDai);
    boolean existsByTaiKhoan_MaTK(String maTK);
    boolean existsByNhanVien_MaNV(String maNV);
}