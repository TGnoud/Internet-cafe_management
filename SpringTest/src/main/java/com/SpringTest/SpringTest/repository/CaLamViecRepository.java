package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, String> {
}