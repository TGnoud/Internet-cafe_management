package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.KhachHang;
import com.SpringTest.SpringTest.entity.LoaiKH;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.KhachHangRepository;
import com.SpringTest.SpringTest.repository.LoaiKHRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.repository.HoaDonDVRepository;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final LoaiKHRepository loaiKHRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhienSuDungRepository phienSuDungRepository;
    private final HoaDonDVRepository hoaDonDVRepository;

    @Autowired
    public TaiKhoanServiceImpl(TaiKhoanRepository taiKhoanRepository,
                              KhachHangRepository khachHangRepository,
                              LoaiKHRepository loaiKHRepository,
                              PasswordEncoder passwordEncoder,
                              PhienSuDungRepository phienSuDungRepository,
                              HoaDonDVRepository hoaDonDVRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.loaiKHRepository = loaiKHRepository;
        this.passwordEncoder = passwordEncoder;
        this.phienSuDungRepository = phienSuDungRepository;
        this.hoaDonDVRepository = hoaDonDVRepository;
    }

    @Override
    public long countAllActiveAccounts() {
        // Đếm các tài khoản được liên kết với một khách hàng (tức là tài khoản khách).
        return taiKhoanRepository.countByKhachHangIsNotNull();
    }

    @Override
    public TaiKhoan findEntityByMaTK(String maTK) {
        return taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + maTK));
    }

    @Override
    public Page<TaiKhoanInfoResponse> getAllKhachHangTaiKhoanPageable(Pageable pageable) {
        try {
            System.out.println("Đang tải danh sách tài khoản...");
            Page<TaiKhoan> customerTaiKhoanPage = taiKhoanRepository.findByKhachHangIsNotNull(pageable);
            
            System.out.println("Số tài khoản tìm thấy: " + customerTaiKhoanPage.getTotalElements());
            
            List<TaiKhoanInfoResponse> dtoList = customerTaiKhoanPage.getContent().stream()
                    .map(taiKhoan -> {
                        try {
                            return mapToTaiKhoanInfoResponse(taiKhoan);
                        } catch (Exception e) {
                            System.err.println("Lỗi khi map tài khoản " + taiKhoan.getMaTK() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            
            System.out.println("Số tài khoản sau khi map: " + dtoList.size());
            
            return new PageImpl<>(dtoList, pageable, customerTaiKhoanPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách tài khoản: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public long countNewAccountsSince(LocalDateTime startOfDay) {
        return 0;
    }

    @Override
    public TaiKhoanInfoResponse getTaiKhoanInfo(String maTK) {
        TaiKhoan taiKhoan = findEntityByMaTK(maTK);
        return mapToTaiKhoanInfoResponse(taiKhoan);
    }

    @Override
    public BigDecimal getSoDuTaiKhoan(String maTK) {
        TaiKhoan taiKhoan = findEntityByMaTK(maTK);
        return taiKhoan.getSoTienConLai();
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse createTaiKhoanKhachHang(CreateTaiKhoanRequest request) {
        if (taiKhoanRepository.findByTenTK(request.getTenTK()).isPresent()) {
            throw new BadRequestException("Tên tài khoản đã tồn tại: " + request.getTenTK());
        }
        if (khachHangRepository.findBySoDienThoai(request.getSoDienThoaiKH()).isPresent()) {
            throw new BadRequestException("Số điện thoại đã được đăng ký: " + request.getSoDienThoaiKH());
        }

        LoaiKH loaiKH = loaiKHRepository.findById(request.getMaLoaiKH())
                .orElseThrow(() -> new ResourceNotFoundException("Loại khách hàng không tồn tại: " + request.getMaLoaiKH()));

        KhachHang khachHang = new KhachHang();
        TaiKhoan taiKhoan = new TaiKhoan();

        // Sử dụng MaKH từ request nếu có, nếu không thì tạo mới (đảm bảo độ dài phù hợp)
        if (request.getMaKH() != null && !request.getMaKH().isEmpty()) {
            if (khachHangRepository.existsById(request.getMaKH())) {
                throw new BadRequestException("Mã khách hàng đã tồn tại: " + request.getMaKH());
            }
            khachHang.setMaKH(request.getMaKH());
        } else {
            khachHang.setMaKH("KH-" + UUID.randomUUID().toString().substring(0, 7)); // Rút ngắn ID
        }

        // Sử dụng MaTK từ request nếu có, nếu không thì tạo mới (đảm bảo độ dài phù hợp)
        if (request.getMaTK() != null && !request.getMaTK().isEmpty()) {
            if (taiKhoanRepository.existsById(request.getMaTK())) {
                throw new BadRequestException("Mã tài khoản đã tồn tại: " + request.getMaTK());
            }
            taiKhoan.setMaTK(request.getMaTK());
        } else {
            taiKhoan.setMaTK("TK-" + UUID.randomUUID().toString().substring(0, 7)); // Rút ngắn ID
        }

        khachHang.setHoTen(request.getHoTenKH());
        khachHang.setSoDienThoai(request.getSoDienThoaiKH());
        khachHang.setGioiTinh(request.getGioiTinhKH());
        khachHang.setLoaiKH(loaiKH);
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        taiKhoan.setTenTK(request.getTenTK());
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau())); // **QUAN TRỌNG: Mã hóa mật khẩu**
        taiKhoan.setSoTienConLai(request.getSoTienNapBanDau() != null ? request.getSoTienNapBanDau() : BigDecimal.ZERO);
        taiKhoan.setKhachHang(savedKhachHang);
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        return mapToTaiKhoanInfoResponse(savedTaiKhoan);
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse napTien(NapTienRequest request) { // Sửa lại để khớp với interface
        if (request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền nạp phải lớn hơn 0.");
        }

        // Lấy maTK từ đối tượng request.
        // Giả định rằng lớp NapTienRequest của bạn có phương thức getMaTK().
        String maTK = request.getMaTK();

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + maTK + " không tồn tại."));

        BigDecimal soTienMoi = taiKhoan.getSoTienConLai().add(request.getSoTien());
        taiKhoan.setSoTienConLai(soTienMoi);

        // Lưu lại tài khoản đã được cập nhật
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        // Mở rộng: Ghi lại giao dịch nạp tiền vào một bảng GiaoDich để đối soát
        // GiaoDich newTx = new GiaoDich(taiKhoan, request.getSoTien(), "Nạp tiền");
        // giaoDichRepository.save(newTx);

        // Trả về đúng kiểu dữ liệu TaiKhoanInfoResponse như interface yêu cầu
        return mapToTaiKhoanInfoResponse(savedTaiKhoan);
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse updateTaiKhoanKhachHang(String maTK, CreateTaiKhoanRequest request) {
        TaiKhoan existingTaiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại: " + maTK));

        KhachHang existingKhachHang = existingTaiKhoan.getKhachHang();
        if (existingKhachHang == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản: " + maTK);
        }

        // Cập nhật thông tin KhachHang
        existingKhachHang.setHoTen(request.getHoTenKH());
        existingKhachHang.setSoDienThoai(request.getSoDienThoaiKH());
        existingKhachHang.setGioiTinh(request.getGioiTinhKH());

        // Cập nhật Loại Khách Hàng
        if (request.getMaLoaiKH() != null && !request.getMaLoaiKH().isEmpty()) {
            LoaiKH loaiKH = loaiKHRepository.findById(request.getMaLoaiKH())
                    .orElseThrow(() -> new ResourceNotFoundException("Loại khách hàng không tồn tại: " + request.getMaLoaiKH()));
            existingKhachHang.setLoaiKH(loaiKH);
        }

        // Cập nhật thông tin TaiKhoan
        existingTaiKhoan.setTenTK(request.getTenTK()); // Cập nhật tên đăng nhập
        existingTaiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau())); // Cập nhật mật khẩu và mã hóa
        if (request.getSoTienConLai() != null) {
            existingTaiKhoan.setSoTienConLai(request.getSoTienConLai()); // Cập nhật số tiền còn lại
        }

        khachHangRepository.save(existingKhachHang);
        taiKhoanRepository.save(existingTaiKhoan); // Lưu lại tài khoản đã được cập nhật

        return mapToTaiKhoanInfoResponse(existingTaiKhoan);
    }

    @Override
    public long count() {
        return taiKhoanRepository.count();
    }

    @Override
    @Transactional
    public void deleteById(String maTK) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại: " + maTK));

        // Kiểm tra xem tài khoản có phiên sử dụng nào không
        if (phienSuDungRepository.existsByTaiKhoan_MaTK(maTK)) {
            throw new BadRequestException("Không thể xóa tài khoản " + maTK + " vì đã có phiên sử dụng. Vui lòng xóa các phiên sử dụng trước.");
        }

        // Kiểm tra xem tài khoản có hóa đơn dịch vụ nào không
        if (hoaDonDVRepository.existsByTaiKhoan_MaTK(maTK)) {
            throw new BadRequestException("Không thể xóa tài khoản " + maTK + " vì đã có hóa đơn dịch vụ. Vui lòng xóa các hóa đơn trước.");
        }

        try {
            // Xóa tài khoản
            taiKhoanRepository.delete(taiKhoan);
            // Xóa khách hàng liên quan
            if (taiKhoan.getKhachHang() != null) {
                khachHangRepository.delete(taiKhoan.getKhachHang());
            }
        } catch (Exception e) {
            throw new BadRequestException("Không thể xóa tài khoản: " + e.getMessage());
        }
    }

    @Override
    public List<TaiKhoan> getAllTaiKhoan() {
        return taiKhoanRepository.findAll();
    }

    private TaiKhoanInfoResponse mapToTaiKhoanInfoResponse(TaiKhoan taiKhoan) {
        try {
            TaiKhoanInfoResponse response = new TaiKhoanInfoResponse();
            response.setMaTK(taiKhoan.getMaTK());
            response.setTenTK(taiKhoan.getTenTK());
            response.setSoTienConLai(taiKhoan.getSoTienConLai());
            
            if (taiKhoan.getKhachHang() != null) {
                KhachHang khachHang = taiKhoan.getKhachHang();
                response.setMaKH(khachHang.getMaKH());
                response.setHoTenKH(khachHang.getHoTen());
                response.setSoDienThoai(khachHang.getSoDienThoai());
                response.setGioiTinh(khachHang.getGioiTinh());
                response.setVaiTro("CUSTOMER");
                response.setTrangThai("ACTIVE");
                if (khachHang.getLoaiKH() != null) {
                    response.setTenLoaiKH(khachHang.getLoaiKH().getTenLoai());
                }
            } else {
                response.setVaiTro("ADMIN");
                response.setTrangThai("ACTIVE");
            }
            
            return response;
        } catch (Exception e) {
            System.err.println("Lỗi khi map tài khoản " + taiKhoan.getMaTK() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteTaiKhoan(String maTK) {
        taiKhoanRepository.deleteById(maTK);
    }

    @Override
    public TaiKhoan updateTaiKhoan(TaiKhoan taiKhoan) {
        if (!taiKhoanRepository.existsById(taiKhoan.getMaTK())) {
            throw new RuntimeException("Không tìm thấy tài khoản với mã: " + taiKhoan.getMaTK());
        }
        return taiKhoanRepository.save(taiKhoan);
    }

    @Override
    public TaiKhoan getTaiKhoanById(Integer maTK) {
        return taiKhoanRepository.findById(maTK.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với mã: " + maTK));
    }

    @Override
    public TaiKhoan findByTenTK(String tenTK) {
        return taiKhoanRepository.findByTenTK(tenTK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với tên: " + tenTK));
    }

    // --- Các phương thức gọi Stored Procedure ---



}