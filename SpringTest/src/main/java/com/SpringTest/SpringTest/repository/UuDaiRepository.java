package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.UuDai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UuDaiRepository extends JpaRepository<UuDai, String> {
}