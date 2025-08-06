package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.UuDai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UuDaiService {
    Page<UuDai> getAllUuDai(Pageable pageable);
    List<UuDai> getAllUuDaiList();
    UuDai getUuDaiById(String maUuDai);
    UuDai saveUuDai(UuDai uuDai); // Hoặc nhận UuDaiFormDTO
    UuDai updateUuDai(String maUuDai, UuDai uuDaiDetails); // Hoặc nhận DTO
    void deleteUuDai(String maUuDai);

    List<UuDai> findAll();

    UuDai save(UuDai uuDai);
}