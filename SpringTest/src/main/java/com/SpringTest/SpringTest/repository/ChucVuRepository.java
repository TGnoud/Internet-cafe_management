package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.ChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucVuRepository extends JpaRepository<ChucVu, String> {
}