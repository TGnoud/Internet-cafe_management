package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.DichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, String> {
    List<DichVu> findByTrangThaiDichVuIgnoreCase(String trangThai);
}