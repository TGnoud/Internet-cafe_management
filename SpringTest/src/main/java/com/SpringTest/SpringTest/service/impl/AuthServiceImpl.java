package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.request.LoginRequest;
import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder; // Sẽ cần khi có security
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private MayTinhRepository mayTinhRepository;

    @Autowired
    private PhienSuDungRepository phienSuDungRepository;

    // @Autowired
    // private PasswordEncoder passwordEncoder; // Sẽ cần khi có security

    @Override
    @Transactional
    public PhienSuDungInfoResponse login(LoginRequest loginRequest) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenTK(loginRequest.getTenTK())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại: " + loginRequest.getTenTK()));

        // TODO: Khi có Spring Security, việc so sánh mật khẩu sẽ dùng passwordEncoder.matches()
        if (!taiKhoan.getMatKhau().equals(loginRequest.getMatKhau())) { // Tạm thời so sánh trực tiếp
            throw new BadRequestException("Mật khẩu không chính xác.");
        }

        if (taiKhoan.getSoTienConLai().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Tài khoản không đủ tiền để sử dụng dịch vụ.");
        }

        MayTinh mayTinh = mayTinhRepository.findById(loginRequest.getMaMay())
                .orElseThrow(() -> new ResourceNotFoundException("Máy tính không tồn tại: " + loginRequest.getMaMay()));

        if (!"Khả dụng".equalsIgnoreCase(mayTinh.getTrangThai())) {
            // Kiểm tra xem có phải chính tài khoản này đang sử dụng máy không (trường hợp re-login/mở khóa)
            PhienSuDung existingSession = null;
            List<PhienSuDung> sessions = phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh);
            if (!sessions.isEmpty()) {
                existingSession = sessions.get(0);
            }
            if (existingSession == null || !existingSession.getTaiKhoan().getMaTK().equals(taiKhoan.getMaTK())) {
                throw new BadRequestException("Máy " + mayTinh.getTenMay() + " hiện không khả dụng hoặc đang được người khác sử dụng.");
            }
            // Nếu là tài khoản này đang dùng thì cho phép, có thể return thông tin phiên cũ
            return mapToPhienSuDungInfoResponse(existingSession, mayTinh.getLoaiMay().getGiaTheoGio());
        }


        // Kiểm tra xem tài khoản này có phiên nào đang mở ở máy khác không
        phienSuDungRepository.findByTaiKhoan_MaTKAndThoiGianKetThucIsNull(taiKhoan.getMaTK())
                .stream()
                .filter(ps -> !ps.getMayTinh().getMaMay().equals(mayTinh.getMaMay()))
                .findFirst()
                .ifPresent(activeSession -> {
                    throw new BadRequestException("Tài khoản " + taiKhoan.getTenTK() +
                            " đang được sử dụng ở máy " + activeSession.getMayTinh().getTenMay() +
                            ". Vui lòng đăng xuất trước khi đăng nhập máy mới.");
                });


        PhienSuDung phienSuDung = new PhienSuDung();
        phienSuDung.setTaiKhoan(taiKhoan);
        phienSuDung.setMayTinh(mayTinh);
        phienSuDung.setThoiGianBatDau(LocalDateTime.now());
        PhienSuDung savedPhien = phienSuDungRepository.save(phienSuDung);

        mayTinh.setTrangThai("Đang sử dụng");
        mayTinhRepository.save(mayTinh);

        return mapToPhienSuDungInfoResponse(savedPhien, mayTinh.getLoaiMay().getGiaTheoGio());
    }

    @Override
    @Transactional
    public void logout(int maPhien) {
        PhienSuDung phienSuDung = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên sử dụng không tồn tại: " + maPhien));

        if (phienSuDung.getThoiGianKetThuc() != null) {
            throw new BadRequestException("Phiên này đã được đăng xuất.");
        }

        phienSuDung.setThoiGianKetThuc(LocalDateTime.now());

        MayTinh mayTinh = phienSuDung.getMayTinh();
        TaiKhoan taiKhoan = phienSuDung.getTaiKhoan();
        BigDecimal giaTheoGio = mayTinh.getLoaiMay().getGiaTheoGio();

        long thoiGianSuDungPhut = ChronoUnit.MINUTES.between(phienSuDung.getThoiGianBatDau(), phienSuDung.getThoiGianKetThuc());
        if (thoiGianSuDungPhut < 1) thoiGianSuDungPhut = 1; // Tính tiền ít nhất 1 phút

        BigDecimal chiPhi = giaTheoGio.multiply(BigDecimal.valueOf(thoiGianSuDungPhut / 60.0));
        // Làm tròn đến 2 chữ số thập phân, hoặc theo đơn vị tiền tệ (ví dụ làm tròn nghìn đồng)
        // chiPhi = chiPhi.setScale(0, RoundingMode.HALF_UP); // Làm tròn đến đơn vị đồng

        BigDecimal soTienConLaiMoi = taiKhoan.getSoTienConLai().subtract(chiPhi);
        if (soTienConLaiMoi.compareTo(BigDecimal.ZERO) < 0) {
            soTienConLaiMoi = BigDecimal.ZERO; // Không cho phép âm tiền (hoặc xử lý nợ)
        }
        taiKhoan.setSoTienConLai(soTienConLaiMoi);
        taiKhoanRepository.save(taiKhoan);

        mayTinh.setTrangThai("Khả dụng");
        mayTinhRepository.save(mayTinh);

        phienSuDungRepository.save(phienSuDung);
    }

    private PhienSuDungInfoResponse mapToPhienSuDungInfoResponse(PhienSuDung phienSuDung, BigDecimal giaTheoGio) {
        PhienSuDungInfoResponse response = new PhienSuDungInfoResponse();
        response.setMaPhien(phienSuDung.getMaPhien());
        response.setMaMay(phienSuDung.getMayTinh().getMaMay());
        response.setTenMay(phienSuDung.getMayTinh().getTenMay());
        response.setMaTK(phienSuDung.getTaiKhoan().getMaTK());
        response.setTenTK(phienSuDung.getTaiKhoan().getTenTK());
        response.setThoiGianBatDau(phienSuDung.getThoiGianBatDau());
        response.setSoTienConLai(phienSuDung.getTaiKhoan().getSoTienConLai());

        if (phienSuDung.getTaiKhoan().getSoTienConLai().compareTo(BigDecimal.ZERO) > 0 && giaTheoGio.compareTo(BigDecimal.ZERO) > 0) {
            // Thời gian còn lại (phút) = Số tiền / (Giá mỗi giờ / 60)
            BigDecimal thoiGianConLaiPhut = phienSuDung.getTaiKhoan().getSoTienConLai()
                    .divide(giaTheoGio.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP), 0, BigDecimal.ROUND_DOWN);
            response.setThoiGianConLaiDuKienPhut(thoiGianConLaiPhut.longValue());
        } else {
            response.setThoiGianConLaiDuKienPhut(0L);
        }
        return response;
    }
}