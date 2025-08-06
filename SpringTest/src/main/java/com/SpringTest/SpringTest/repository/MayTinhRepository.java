package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.MayTinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MayTinhRepository extends JpaRepository<MayTinh, String> {
    List<MayTinh> findByTrangThai(String trangThai);
    List<MayTinh> findByLoaiMay_MaLoaiMay(String maLoaiMay);
    @Query(value = "SELECT KiemTraTrangThaiMayTinh(:maMay)", nativeQuery = true)
    String getKiemTraTrangThaiMayTinh(@Param("maMay") Integer maMay);
    long countByTrangThai(String trangThai);
}