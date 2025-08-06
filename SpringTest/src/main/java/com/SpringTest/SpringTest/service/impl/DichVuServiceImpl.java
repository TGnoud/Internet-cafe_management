package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;
import com.SpringTest.SpringTest.entity.*;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.ChiTietHoaDonDVRepository;
import com.SpringTest.SpringTest.repository.DichVuRepository;
import com.SpringTest.SpringTest.repository.HoaDonDVRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.DichVuService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DichVuServiceImpl implements DichVuService {

    @Autowired
    private DichVuRepository dichVuRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private HoaDonDVRepository hoaDonDVRepository; // Sẽ dùng nếu tạo hóa đơn ngay

    @Autowired
    private ChiTietHoaDonDVRepository chiTietHoaDonDVRepository; // Sẽ dùng nếu tạo hóa đơn ngay

    // Mapper (có thể dùng MapStruct hoặc làm thủ công)
    private DichVuDTO toDTO(DichVu dichVu) {
        DichVuDTO dto = new DichVuDTO();
        BeanUtils.copyProperties(dichVu, dto);
        return dto;
    }

    private DichVu toEntity(DichVuDTO dto) {
        DichVu entity = new DichVu();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional
    public void customerOrderService(OrderServiceRequest request, String maTKCurrentUser) {
        // Xác thực maTK từ request phải là của current user (nếu cần)
        // Hoặc maTK trong request là maTK của người dùng đang đăng nhập
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTKCurrentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + maTKCurrentUser + " không tồn tại."));

        BigDecimal tongTienOrder = BigDecimal.ZERO;
        for (OrderServiceRequest.OrderItemRequest item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + item.getMaDV() + " không tồn tại."));
            if (!"Còn hàng".equalsIgnoreCase(dichVu.getTrangThaiDichVu())) { // Hoặc kiểm tra số lượng tồn
                throw new BadRequestException("Dịch vụ " + dichVu.getTenDV() + " hiện không khả dụng.");
            }
            tongTienOrder = tongTienOrder.add(dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        if (taiKhoan.getSoTienConLai().compareTo(tongTienOrder) < 0) {
            throw new BadRequestException("Số dư tài khoản không đủ để thực hiện giao dịch này.");
        }

        // Trừ tiền tài khoản và tạo hóa đơn (đơn giản hóa: tạo hóa đơn ngay)
        // Trong thực tế, có thể có bước nhân viên xác nhận và lập hóa đơn sau
        taiKhoan.setSoTienConLai(taiKhoan.getSoTienConLai().subtract(tongTienOrder));
        taiKhoanRepository.save(taiKhoan);

        HoaDonDV hoaDonDV = new HoaDonDV();
        hoaDonDV.setMaHD("HD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        hoaDonDV.setTaiKhoan(taiKhoan);
        hoaDonDV.setThoiDiemThanhToan(LocalDateTime.now());
        // hoaDonDV.setNhanVien(null); // Nếu khách tự order, có thể không có NV hoặc NV hệ thống
        // hoaDonDV.setUuDai(null); // Xử lý ưu đãi nếu có
        HoaDonDV savedHoaDon = hoaDonDVRepository.save(hoaDonDV);

        for (OrderServiceRequest.OrderItemRequest item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV()).get(); // Đã kiểm tra ở trên
            ChiTietHoaDonDVId ctId = new ChiTietHoaDonDVId(savedHoaDon.getMaHD(), dichVu.getMaDV());
            ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
            chiTiet.setId(ctId);
            chiTiet.setHoaDonDV(savedHoaDon);
            chiTiet.setDichVu(dichVu);
            chiTiet.setSoLuong(item.getSoLuong());
            // chiTiet.setDonGiaLucBan(dichVu.getDonGia()); // Cần thêm cột này vào Entity CT_HoaDonDV
            // chiTiet.setThanhTien(dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()))); // Cần thêm cột này
            chiTietHoaDonDVRepository.save(chiTiet);
        }
        // Logic thông báo cho nhân viên (ví dụ qua WebSocket hoặc hàng đợi) có thể được thêm ở đây
    }

    @Override
    @Transactional
    public DichVuDTO addDichVu(DichVuDTO dichVuDTO) {
        if (dichVuRepository.existsById(dichVuDTO.getMaDV())) {
            throw new BadRequestException("Mã dịch vụ " + dichVuDTO.getMaDV() + " đã tồn tại.");
        }
        DichVu dichVu = toEntity(dichVuDTO);
        if (dichVu.getTrangThaiDichVu() == null || dichVu.getTrangThaiDichVu().isEmpty()) {
            dichVu.setTrangThaiDichVu("Còn hàng"); // Mặc định
        } else if (!dichVu.getTrangThaiDichVu().equalsIgnoreCase("Còn hàng") && 
                  !dichVu.getTrangThaiDichVu().equalsIgnoreCase("Hết hàng") &&
                  !dichVu.getTrangThaiDichVu().equalsIgnoreCase("Sắp có")) {
            throw new BadRequestException("Trạng thái dịch vụ không hợp lệ. Chỉ chấp nhận 'Còn hàng', 'Hết hàng' hoặc 'Sắp có'.");
        }
        return toDTO(dichVuRepository.save(dichVu));
    }

    @Override
    @Transactional
    public DichVuDTO updateDichVu(String maDV, DichVuDTO dichVuDTO) {
        DichVu existingDichVu = dichVuRepository.findById(maDV)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + maDV + " không tìm thấy."));

        existingDichVu.setTenDV(dichVuDTO.getTenDV());
        existingDichVu.setDonGia(dichVuDTO.getDonGia());
        if (dichVuDTO.getTrangThaiDichVu() != null && !dichVuDTO.getTrangThaiDichVu().isEmpty()){
            if (!dichVuDTO.getTrangThaiDichVu().equalsIgnoreCase("Còn hàng") && 
                !dichVuDTO.getTrangThaiDichVu().equalsIgnoreCase("Hết hàng") &&
                !dichVuDTO.getTrangThaiDichVu().equalsIgnoreCase("Sắp có")) {
                throw new BadRequestException("Trạng thái dịch vụ không hợp lệ. Chỉ chấp nhận 'Còn hàng', 'Hết hàng' hoặc 'Sắp có'.");
            }
            existingDichVu.setTrangThaiDichVu(dichVuDTO.getTrangThaiDichVu());
        }
        // Không cho cập nhật MaDV
        return toDTO(dichVuRepository.save(existingDichVu));
    }

    @Override
    public DichVuDTO getDichVuByMaDV(String maDV) {
        return dichVuRepository.findById(maDV).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + maDV + " không tìm thấy."));
    }

    @Override
    public List<DichVuDTO> getAllDichVu() {
        return dichVuRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DichVuDTO> findAvailableDichVu() {
        return dichVuRepository.findByTrangThaiDichVuIgnoreCase("Còn hàng")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDichVu(String maDV) {
        DichVu dichVu = dichVuRepository.findById(maDV)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + maDV + " không tìm thấy."));

        // Kiểm tra xem dịch vụ có trong bất kỳ chi tiết hóa đơn nào không
        if (chiTietHoaDonDVRepository.existsByDichVu_MaDV(maDV)) {
            throw new BadRequestException("Không thể xóa dịch vụ " + maDV + " vì đã tồn tại trong các hóa đơn. Cân nhắc đổi trạng thái thành 'Hết hàng'.");
        }
        dichVuRepository.delete(dichVu);
    }

    @Override
    public long count() {
        return dichVuRepository.count();
    }
}