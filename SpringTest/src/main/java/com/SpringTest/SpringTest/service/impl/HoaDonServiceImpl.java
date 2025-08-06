package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.HoaDonDTO;
import com.SpringTest.SpringTest.dto.request.CreateHoaDonRequest;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;
import com.SpringTest.SpringTest.entity.*;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.*;
import com.SpringTest.SpringTest.service.HoaDonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
// Sửa lỗi: Implement đúng interface HoaDonService
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonDVRepository hoaDonDVRepository;
    @Autowired
    private ChiTietHoaDonDVRepository chiTietHoaDonDVRepository;
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private DichVuRepository dichVuRepository;
    @Autowired
    private UuDaiRepository uuDaiRepository;

    @Override
    @Transactional
    // Sửa lỗi: Implement đúng phương thức createHoaDon theo interface
    public HoaDonDTO createHoaDon(CreateHoaDonRequest request) {
        // 1. Lấy các đối tượng liên quan từ DB
        NhanVien nhanVien = nhanVienRepository.findById(request.getMaNV())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên " + request.getMaNV() + " không tồn tại."));

        TaiKhoan taiKhoan = null;
        if (request.getMaTK() != null && !request.getMaTK().isEmpty()) {
            taiKhoan = taiKhoanRepository.findById(request.getMaTK())
                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + request.getMaTK() + " không tồn tại."));
        }

        UuDai uuDai = null;
        if (request.getMaUuDai() != null && !request.getMaUuDai().isEmpty()) {
            uuDai = uuDaiRepository.findById(request.getMaUuDai())
                    .orElseThrow(() -> new ResourceNotFoundException("Ưu đãi " + request.getMaUuDai() + " không tồn tại."));
        }

        // 2. Tạo và lưu hóa đơn trước để lấy MaHD
        HoaDonDV hoaDonDV = new HoaDonDV();
        hoaDonDV.setMaHD("HD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        hoaDonDV.setThoiDiemThanhToan(LocalDateTime.now());
        hoaDonDV.setNhanVien(nhanVien);
        hoaDonDV.setTaiKhoan(taiKhoan);
        hoaDonDV.setUuDai(uuDai);

        HoaDonDV savedHoaDon = hoaDonDVRepository.save(hoaDonDV);

        // 3. Tạo các chi tiết hóa đơn và tính tổng tiền
        BigDecimal tongTienTruocGiam = BigDecimal.ZERO;
        List<ChiTietHoaDonDV> chiTietList = new ArrayList<>();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Hóa đơn phải có ít nhất một dịch vụ.");
        }

        for (OrderServiceRequest.OrderItemRequest item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + item.getMaDV() + " không tồn tại."));

            if (!"Còn hàng".equalsIgnoreCase(dichVu.getTrangThaiDichVu())) {
                throw new BadRequestException("Dịch vụ " + dichVu.getTenDV() + " hiện không khả dụng.");
            }

            ChiTietHoaDonDVId ctId = new ChiTietHoaDonDVId(savedHoaDon.getMaHD(), dichVu.getMaDV());
            ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
            chiTiet.setId(ctId);
            chiTiet.setHoaDonDV(savedHoaDon);
            chiTiet.setDichVu(dichVu);
            chiTiet.setSoLuong(item.getSoLuong());

            chiTietList.add(chiTiet);

            BigDecimal thanhTienItem = dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
            tongTienTruocGiam = tongTienTruocGiam.add(thanhTienItem);
        }
        chiTietHoaDonDVRepository.saveAll(chiTietList);

        // 4. Tính toán giảm giá và tổng cuối cùng
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (uuDai != null && uuDai.getMucUuDai() != null) {
            soTienGiam = tongTienTruocGiam.multiply(uuDai.getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
        }
        BigDecimal tongTienSauGiam = tongTienTruocGiam.subtract(soTienGiam);

        // 5. Xử lý thanh toán qua tài khoản (nếu có)
        if (taiKhoan != null) {
            System.out.println("Số tiền trước khi trừ: " + taiKhoan.getSoTienConLai());
            System.out.println("Tổng tiền trước giảm giá: " + tongTienTruocGiam);
            System.out.println("Số tiền giảm giá: " + soTienGiam);
            System.out.println("Tổng tiền sau giảm giá: " + tongTienSauGiam);
            
            // Kiểm tra xem có dịch vụ đặc biệt không
            boolean hasSpecialService = request.getItems().stream()
                .anyMatch(item -> item.getMaDV().equals("DV001") || 
                                item.getMaDV().equals("DV002") || 
                                item.getMaDV().equals("DV003"));
            
            if (hasSpecialService) {
                // Nếu có dịch vụ đặc biệt, cộng tiền vào tài khoản
                BigDecimal soTienConLaiMoi = taiKhoan.getSoTienConLai().add(tongTienSauGiam);
                System.out.println("Số tiền còn lại mới (cộng): " + soTienConLaiMoi);
                taiKhoan.setSoTienConLai(soTienConLaiMoi);
            } else {
                // Nếu là dịch vụ thông thường, kiểm tra số dư và trừ tiền
                if (taiKhoan.getSoTienConLai().compareTo(tongTienSauGiam) < 0) {
                    throw new BadRequestException("Số dư tài khoản " + taiKhoan.getTenTK() + " không đủ. Cần " + tongTienSauGiam);
                }
                BigDecimal soTienConLaiMoi = taiKhoan.getSoTienConLai().subtract(tongTienSauGiam);
                System.out.println("Số tiền còn lại mới (trừ): " + soTienConLaiMoi);
                taiKhoan.setSoTienConLai(soTienConLaiMoi);
            }
            taiKhoanRepository.save(taiKhoan);
        }

        // Trả về DTO theo đúng yêu cầu của interface
        return mapToHoaDonDTO(savedHoaDon, chiTietList, tongTienTruocGiam, soTienGiam, tongTienSauGiam);
    }

    @Override
    public HoaDonDTO getHoaDonById(String maHD) {
        HoaDonDV hoaDonDV = hoaDonDVRepository.findById(maHD)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn " + maHD + " không tìm thấy."));
        return mapToHoaDonDTO(hoaDonDV);
    }

    @Override
    public List<HoaDonDTO> getHoaDonByMaTK(String maTK) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + maTK + " không tìm thấy."));
        return hoaDonDVRepository.findByTaiKhoan(taiKhoan).stream()
                .map(this::mapToHoaDonDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HoaDonDTO> getHoaDonByMaNV(String maNV) {
        NhanVien nhanVien = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên " + maNV + " không tồn tại."));
        return hoaDonDVRepository.findByNhanVien(nhanVien).stream()
                .map(this::mapToHoaDonDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<HoaDonDTO> getAllHoaDon(Pageable pageable) {
        return hoaDonDVRepository.findAll(pageable)
                .map(this::mapToHoaDonDTO);
    }

    @Override
    public HoaDonDV createHoaDonDV(CreateHoaDonRequest request) {
        return null;
    }

    @Override
    public BigDecimal calculateBillTotal(String maHD, boolean applyDiscount) {
        return null;
    }

    @Override
    @Transactional
    public HoaDonDTO updateHoaDon(String maHD, CreateHoaDonRequest request) {
        // 1. Kiểm tra hóa đơn tồn tại
        HoaDonDV hoaDonDV = hoaDonDVRepository.findById(maHD)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn " + maHD + " không tồn tại."));

        // 2. Lấy các đối tượng liên quan từ DB
        NhanVien nhanVien = nhanVienRepository.findById(request.getMaNV())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên " + request.getMaNV() + " không tồn tại."));

        TaiKhoan taiKhoan = null;
        if (request.getMaTK() != null && !request.getMaTK().isEmpty()) {
            taiKhoan = taiKhoanRepository.findById(request.getMaTK())
                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + request.getMaTK() + " không tồn tại."));
        }

        UuDai uuDai = null;
        if (request.getMaUuDai() != null && !request.getMaUuDai().isEmpty()) {
            uuDai = uuDaiRepository.findById(request.getMaUuDai())
                    .orElseThrow(() -> new ResourceNotFoundException("Ưu đãi " + request.getMaUuDai() + " không tồn tại."));
        }

        // 3. Cập nhật thông tin hóa đơn
        hoaDonDV.setNhanVien(nhanVien);
        hoaDonDV.setTaiKhoan(taiKhoan);
        hoaDonDV.setUuDai(uuDai);
        hoaDonDV.setThoiDiemThanhToan(LocalDateTime.now());

        // 4. Xóa các chi tiết hóa đơn cũ
        List<ChiTietHoaDonDV> chiTietCu = chiTietHoaDonDVRepository.findByHoaDonDV(hoaDonDV);
        chiTietHoaDonDVRepository.deleteAll(chiTietCu);

        // 5. Tạo các chi tiết hóa đơn mới và tính tổng tiền
        BigDecimal tongTienTruocGiam = BigDecimal.ZERO;
        List<ChiTietHoaDonDV> chiTietList = new ArrayList<>();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Hóa đơn phải có ít nhất một dịch vụ.");
        }

        for (OrderServiceRequest.OrderItemRequest item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + item.getMaDV() + " không tồn tại."));

            if (!"Còn hàng".equalsIgnoreCase(dichVu.getTrangThaiDichVu())) {
                throw new BadRequestException("Dịch vụ " + dichVu.getTenDV() + " hiện không khả dụng.");
            }

            ChiTietHoaDonDVId ctId = new ChiTietHoaDonDVId(hoaDonDV.getMaHD(), dichVu.getMaDV());
            ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
            chiTiet.setId(ctId);
            chiTiet.setHoaDonDV(hoaDonDV);
            chiTiet.setDichVu(dichVu);
            chiTiet.setSoLuong(item.getSoLuong());

            chiTietList.add(chiTiet);

            BigDecimal thanhTienItem = dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
            tongTienTruocGiam = tongTienTruocGiam.add(thanhTienItem);
        }
        chiTietHoaDonDVRepository.saveAll(chiTietList);

        // 6. Tính toán giảm giá và tổng cuối cùng
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (uuDai != null && uuDai.getMucUuDai() != null) {
            soTienGiam = tongTienTruocGiam.multiply(uuDai.getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
        }
        BigDecimal tongTienSauGiam = tongTienTruocGiam.subtract(soTienGiam);

        // 7. Xử lý thanh toán qua tài khoản (nếu có)
        if (taiKhoan != null) {
            System.out.println("Số tiền trước khi trừ: " + taiKhoan.getSoTienConLai());
            System.out.println("Tổng tiền trước giảm giá: " + tongTienTruocGiam);
            System.out.println("Số tiền giảm giá: " + soTienGiam);
            System.out.println("Tổng tiền sau giảm giá: " + tongTienSauGiam);
            
            // Kiểm tra xem có dịch vụ đặc biệt không
            boolean hasSpecialService = request.getItems().stream()
                .anyMatch(item -> item.getMaDV().equals("DV001") || 
                                item.getMaDV().equals("DV002") || 
                                item.getMaDV().equals("DV003"));
            
            if (hasSpecialService) {
                // Nếu có dịch vụ đặc biệt, cộng tiền vào tài khoản
                BigDecimal soTienConLaiMoi = taiKhoan.getSoTienConLai().add(tongTienSauGiam);
                System.out.println("Số tiền còn lại mới (cộng): " + soTienConLaiMoi);
                taiKhoan.setSoTienConLai(soTienConLaiMoi);
            } else {
                // Nếu là dịch vụ thông thường, kiểm tra số dư và trừ tiền
                if (taiKhoan.getSoTienConLai().compareTo(tongTienSauGiam) < 0) {
                    throw new BadRequestException("Số dư tài khoản " + taiKhoan.getTenTK() + " không đủ. Cần " + tongTienSauGiam);
                }
                BigDecimal soTienConLaiMoi = taiKhoan.getSoTienConLai().subtract(tongTienSauGiam);
                System.out.println("Số tiền còn lại mới (trừ): " + soTienConLaiMoi);
                taiKhoan.setSoTienConLai(soTienConLaiMoi);
            }
            taiKhoanRepository.save(taiKhoan);
        }

        // 8. Lưu hóa đơn đã cập nhật
        HoaDonDV savedHoaDon = hoaDonDVRepository.save(hoaDonDV);

        // 9. Trả về DTO
        return mapToHoaDonDTO(savedHoaDon, chiTietList, tongTienTruocGiam, soTienGiam, tongTienSauGiam);
    }

    // Helper method để map sang DTO
    private HoaDonDTO mapToHoaDonDTO(HoaDonDV hoaDonDV) {
        List<ChiTietHoaDonDV> chiTietList = chiTietHoaDonDVRepository.findByHoaDonDV(hoaDonDV);

        BigDecimal tongTienTruocGiam = chiTietList.stream()
                .map(ct -> ct.getDichVu().getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (hoaDonDV.getUuDai() != null && hoaDonDV.getUuDai().getMucUuDai() != null) {
            soTienGiam = tongTienTruocGiam.multiply(hoaDonDV.getUuDai().getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
        }
        BigDecimal tongTienSauGiam = tongTienTruocGiam.subtract(soTienGiam);

        HoaDonDTO dto = new HoaDonDTO();
        dto.setMaHD(hoaDonDV.getMaHD());
        dto.setThoiDiemThanhToan(hoaDonDV.getThoiDiemThanhToan());

        if (hoaDonDV.getTaiKhoan() != null) {
            dto.setMaTK(hoaDonDV.getTaiKhoan().getMaTK());
            dto.setTenTK(hoaDonDV.getTaiKhoan().getTenTK());
        }
        if (hoaDonDV.getNhanVien() != null) {
            dto.setMaNV(hoaDonDV.getNhanVien().getMaNV());
            dto.setTenNV(hoaDonDV.getNhanVien().getHoTen());
        }
        if (hoaDonDV.getUuDai() != null) {
            dto.setMaUuDai(hoaDonDV.getUuDai().getMaUuDai());
            dto.setNoiDungUuDai(hoaDonDV.getUuDai().getNoiDung());
            dto.setMucUuDai(hoaDonDV.getUuDai().getMucUuDai());
        }

        // Map thông tin chi tiết hóa đơn
        List<HoaDonDTO.ChiTietHoaDonDTO> chiTietDTOs = chiTietList.stream().map(ct -> {
            HoaDonDTO.ChiTietHoaDonDTO ctDto = new HoaDonDTO.ChiTietHoaDonDTO();
            ctDto.setMaDV(ct.getDichVu().getMaDV());
            ctDto.setTenDV(ct.getDichVu().getTenDV());
            ctDto.setSoLuong(ct.getSoLuong());
            BigDecimal donGia = ct.getDichVu().getDonGia();
            ctDto.setDonGiaLucBan(donGia);
            
            // Tính thành tiền trước giảm giá
            BigDecimal thanhTienTruocGiam = donGia.multiply(BigDecimal.valueOf(ct.getSoLuong()));
            
            // Tính thành tiền sau giảm giá
            BigDecimal thanhTienSauGiam = thanhTienTruocGiam;
            if (hoaDonDV.getUuDai() != null && hoaDonDV.getUuDai().getMucUuDai() != null) {
                BigDecimal tienGiam = thanhTienTruocGiam.multiply(hoaDonDV.getUuDai().getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
                thanhTienSauGiam = thanhTienTruocGiam.subtract(tienGiam);
            }
            
            ctDto.setThanhTien(thanhTienSauGiam);
            return ctDto;
        }).collect(Collectors.toList());
        dto.setChiTiet(chiTietDTOs);

        // Map thông tin hiển thị trong danh sách
        if (!chiTietList.isEmpty()) {
            ChiTietHoaDonDV firstItem = chiTietList.get(0);
            dto.setTenDichVu(firstItem.getDichVu().getTenDV());
            dto.setDonGia(firstItem.getDichVu().getDonGia());
            dto.setSoLuong(firstItem.getSoLuong());
            
            // Tính thành tiền cho item đầu tiên
            BigDecimal thanhTienTruocGiam = firstItem.getDichVu().getDonGia().multiply(BigDecimal.valueOf(firstItem.getSoLuong()));
            BigDecimal thanhTienSauGiam = thanhTienTruocGiam;
            if (hoaDonDV.getUuDai() != null && hoaDonDV.getUuDai().getMucUuDai() != null) {
                BigDecimal tienGiam = thanhTienTruocGiam.multiply(hoaDonDV.getUuDai().getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
                thanhTienSauGiam = thanhTienTruocGiam.subtract(tienGiam);
            }
            dto.setThanhTien(thanhTienSauGiam);
        }

        dto.setTongTienTruocGiam(tongTienTruocGiam);
        dto.setSoTienGiam(soTienGiam);
        dto.setTongTienSauGiam(tongTienSauGiam);

        return dto;
    }

    // Overloaded helper method
    private HoaDonDTO mapToHoaDonDTO(HoaDonDV hoaDonDV, List<ChiTietHoaDonDV> chiTietList, BigDecimal ttTruocGiam, BigDecimal tienGiam, BigDecimal ttSauGiam) {
        HoaDonDTO dto = new HoaDonDTO();
        BeanUtils.copyProperties(hoaDonDV, dto);

        if (hoaDonDV.getTaiKhoan() != null) {
            dto.setMaTK(hoaDonDV.getTaiKhoan().getMaTK());
            dto.setTenTK(hoaDonDV.getTaiKhoan().getTenTK());
        }
        if (hoaDonDV.getNhanVien() != null) {
            dto.setMaNV(hoaDonDV.getNhanVien().getMaNV());
            dto.setTenNV(hoaDonDV.getNhanVien().getHoTen());
        }
        if (hoaDonDV.getUuDai() != null) {
            dto.setMaUuDai(hoaDonDV.getUuDai().getMaUuDai());
            dto.setNoiDungUuDai(hoaDonDV.getUuDai().getNoiDung());
            dto.setMucUuDai(hoaDonDV.getUuDai().getMucUuDai());
        }

        List<HoaDonDTO.ChiTietHoaDonDTO> chiTietDTOs = chiTietList.stream().map(ct -> {
            HoaDonDTO.ChiTietHoaDonDTO ctDto = new HoaDonDTO.ChiTietHoaDonDTO();
            ctDto.setMaDV(ct.getDichVu().getMaDV());
            ctDto.setTenDV(ct.getDichVu().getTenDV());
            ctDto.setSoLuong(ct.getSoLuong());
            BigDecimal donGia = ct.getDichVu().getDonGia();
            ctDto.setDonGiaLucBan(donGia);
            
            // Tính thành tiền trước giảm giá
            BigDecimal thanhTienTruocGiam = donGia.multiply(BigDecimal.valueOf(ct.getSoLuong()));
            
            // Tính thành tiền sau giảm giá
            BigDecimal thanhTienSauGiam = thanhTienTruocGiam;
            if (hoaDonDV.getUuDai() != null && hoaDonDV.getUuDai().getMucUuDai() != null) {
                BigDecimal tienGiamItem = thanhTienTruocGiam.multiply(hoaDonDV.getUuDai().getMucUuDai().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
                thanhTienSauGiam = thanhTienTruocGiam.subtract(tienGiamItem);
            }
            
            ctDto.setThanhTien(thanhTienSauGiam);
            return ctDto;
        }).collect(Collectors.toList());
        dto.setChiTiet(chiTietDTOs);

        dto.setTongTienTruocGiam(ttTruocGiam);
        dto.setSoTienGiam(tienGiam);
        dto.setTongTienSauGiam(ttSauGiam);

        return dto;
    }


}