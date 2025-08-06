package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhienSuDungServiceImpl implements PhienSuDungService {

    @Autowired
    private PhienSuDungRepository phienSuDungRepository;

    @Autowired
    private MayTinhRepository mayTinhRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional
    public PhienSuDung batDauPhienSuDung(String maMay, String maTK) {
        MayTinh mayTinh = mayTinhRepository.findById(maMay)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy máy tính với mã: " + maMay));

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với mã: " + maTK));

        if (!mayTinh.getTrangThai().equals("Khả dụng")) {
            throw new BadRequestException("Máy tính không khả dụng");
        }

        PhienSuDung phien = new PhienSuDung();
        phien.setMayTinh(mayTinh);
        phien.setTaiKhoan(taiKhoan);
        phien.setThoiGianBatDau(LocalDateTime.now());

        mayTinh.setTrangThai("Đang sử dụng");
        mayTinhRepository.save(mayTinh);

        return phienSuDungRepository.save(phien);
    }

    @Override
    @Transactional
    public PhienSuDung ketThucPhienSuDung(Integer maPhien) {
        PhienSuDung phienSuDung = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên sử dụng với mã: " + maPhien));

        if (phienSuDung.getThoiGianKetThuc() != null) {
            throw new BadRequestException("Phiên sử dụng đã kết thúc");
        }

        LocalDateTime thoiGianKetThuc = LocalDateTime.now();
        phienSuDung.setThoiGianKetThuc(thoiGianKetThuc);

        MayTinh mayTinh = phienSuDung.getMayTinh();
        mayTinh.setTrangThai("Khả dụng");
        mayTinhRepository.save(mayTinh);

        return phienSuDungRepository.save(phienSuDung);
    }

    @Override
    public List<PhienSuDung> getPhienSuDungByTaiKhoan(String maTK) {
        return phienSuDungRepository.findByTaiKhoan_MaTK(maTK);
    }

    @Override
    public long tinhThoiGianConLaiPhut(Integer maPhien) {
        PhienSuDung phienSuDung = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên sử dụng với mã: " + maPhien));

        if (phienSuDung.getThoiGianKetThuc() != null) {
            return 0;
        }

        LocalDateTime thoiGianBatDau = phienSuDung.getThoiGianBatDau();
        LocalDateTime thoiGianHienTai = LocalDateTime.now();
        long thoiGianConLaiPhut = ChronoUnit.MINUTES.between(thoiGianHienTai, thoiGianBatDau.plusHours(2));

        return Math.max(0, thoiGianConLaiPhut);
    }

    @Override
    public long countByTrangThai(String trangThai) {
        if ("Đang hoạt động".equals(trangThai)) {
            return phienSuDungRepository.countByThoiGianKetThucIsNull();
        }
        return 0;
    }

    @Override
    public List<PhienSuDung> findAll() {
        List<PhienSuDung> sessions = phienSuDungRepository.findAll();
        System.out.println("Number of sessions retrieved: " + sessions.size());
        return sessions;
    }

    @Override
    public PhienSuDung findById(Integer id) {
        return phienSuDungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên sử dụng với id: " + id));
    }

    @Override
    public PhienSuDung save(PhienSuDung phienSuDung) {
        return phienSuDungRepository.save(phienSuDung);
    }

    @Override
    public void deleteById(Integer id) {
        phienSuDungRepository.deleteById(id);
    }

    @Override
    public long countByThoiGianKetThucIsNull() {
        return phienSuDungRepository.countByThoiGianKetThucIsNull();
    }

    @Override
    public Page<PhienSuDung> findAll(Pageable pageable) {
        return phienSuDungRepository.findAll(pageable);
    }

    @Override
    public Page<PhienSuDung> getAllPhienSuDung(Pageable pageable) {
        return phienSuDungRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public PhienSuDung createPhienSuDung(String maMay, String maTK) {
        MayTinh mayTinh = mayTinhRepository.findById(maMay)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy máy tính với mã: " + maMay));

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với mã: " + maTK));

        if (mayTinh == null) {
            throw new RuntimeException("Không tìm thấy máy tính");
        }
        if (taiKhoan == null) {
            throw new RuntimeException("Không tìm thấy tài khoản");
        }

        if (phienSuDungRepository.existsByMayTinh_MaMayAndThoiGianKetThucIsNull(maMay)) {
            throw new RuntimeException("Máy tính này đang được sử dụng");
        }

        PhienSuDung phien = new PhienSuDung();
        phien.setMayTinh(mayTinh);
        phien.setTaiKhoan(taiKhoan);
        phien.setThoiGianBatDau(LocalDateTime.now());
        
        mayTinh.setTrangThai("Đang sử dụng");
        mayTinhRepository.save(mayTinh);

        return phienSuDungRepository.save(phien);
    }

    @Override
    @Transactional
    public void endPhienSuDung(String maPhien) {
        PhienSuDung phien = phienSuDungRepository.findById(Integer.parseInt(maPhien))
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên sử dụng"));

        if (phien.getThoiGianKetThuc() != null) {
            throw new RuntimeException("Phiên sử dụng đã kết thúc");
        }

        phien.setThoiGianKetThuc(LocalDateTime.now());
        phienSuDungRepository.save(phien);

        // Cập nhật trạng thái máy
        MayTinh mayTinh = phien.getMayTinh();
        mayTinh.setTrangThai("Khả dụng");
        mayTinhRepository.save(mayTinh);
    }

    @Override
    public void deletePhienSuDung(Integer maPhien) {
        PhienSuDung phien = findById(maPhien);
        if (phien == null) {
            throw new RuntimeException("Không tìm thấy phiên sử dụng với mã: " + maPhien);
        }
        
        // Nếu phiên đang hoạt động, không cho phép xóa
        if (phien.getThoiGianKetThuc() == null) {
            throw new RuntimeException("Không thể xóa phiên đang hoạt động");
        }
        
        phienSuDungRepository.delete(phien);
    }
}