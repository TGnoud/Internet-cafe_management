package com.SpringTest.SpringTest.repository;


import com.SpringTest.SpringTest.entity.LoaiKH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiKHRepository extends JpaRepository<LoaiKH, String> { // Kiểu khóa chính là String
}