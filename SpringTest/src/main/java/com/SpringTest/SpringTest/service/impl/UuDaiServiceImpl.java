package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.UuDai;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.UuDaiRepository;
import com.SpringTest.SpringTest.service.UuDaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UuDaiServiceImpl implements UuDaiService {

    @Autowired
    private UuDaiRepository uuDaiRepository;

    @Override
    public Page<UuDai> getAllUuDai(Pageable pageable) {
        return uuDaiRepository.findAll(pageable);
    }

    @Override
    public List<UuDai> getAllUuDaiList() {
        return uuDaiRepository.findAll();
    }

    @Override
    public UuDai getUuDaiById(String maUuDai) {
        return uuDaiRepository.findById(maUuDai)
                .orElseThrow(() -> new ResourceNotFoundException("Ưu đãi không tồn tại: " + maUuDai));
    }

    @Override
    @Transactional
    public UuDai saveUuDai(UuDai uuDai) { // Nên nhận DTO
        if (uuDaiRepository.existsById(uuDai.getMaUuDai())) {
            throw new BadRequestException("Mã ưu đãi " + uuDai.getMaUuDai() + " đã tồn tại.");
        }
        // Thêm validation cho các trường khác của UuDai nếu cần
        return uuDaiRepository.save(uuDai);
    }

    @Override
    @Transactional
    public UuDai updateUuDai(String maUuDai, UuDai uuDaiDetails) { // Nên nhận DTO
        UuDai existingUuDai = getUuDaiById(maUuDai);
        existingUuDai.setNoiDung(uuDaiDetails.getNoiDung());
        existingUuDai.setMucUuDai(uuDaiDetails.getMucUuDai());
        // Cập nhật các trường khác nếu có (ngày bắt đầu, kết thúc,...)
        return uuDaiRepository.save(existingUuDai);
    }

    @Override
    @Transactional
    public void deleteUuDai(String maUuDai) {
        UuDai uuDai = getUuDaiById(maUuDai);
        // Kiểm tra ràng buộc, ví dụ: ưu đãi có đang được áp dụng ở đâu không
        // if (loaiKHRepository.existsByUuDai(uuDai) || hoaDonDVRepository.existsByUuDai(uuDai)) {
        //    throw new BadRequestException("Không thể xóa ưu đãi vì đang được sử dụng.");
        // }
        uuDaiRepository.delete(uuDai);
    }

    @Override
    public List<UuDai> findAll() {
        return uuDaiRepository.findAll();
    }

    @Override
    public UuDai save(UuDai uuDai) {
        return uuDaiRepository.save(uuDai);
    }

}