package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.dto.HoaDonDTO;
import com.SpringTest.SpringTest.dto.request.CreateHoaDonRequest;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;
import com.SpringTest.SpringTest.entity.HoaDonDV;
import com.SpringTest.SpringTest.entity.ChiTietHoaDonDV;
import com.SpringTest.SpringTest.entity.ChiTietHoaDonDVId;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.service.HoaDonService;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import com.SpringTest.SpringTest.service.DichVuService;
import com.SpringTest.SpringTest.service.UuDaiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/service-bills")
public class ServiceBillController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceBillController.class);

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private UuDaiService uuDaiService;

    @GetMapping
    public String listServiceBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDonDTO> serviceBillPage = hoaDonService.getAllHoaDon(pageable);
        model.addAttribute("serviceBillPage", serviceBillPage);
        return "admin/manage-service-bills";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        HoaDonDV hoaDonDV = new HoaDonDV();
        ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
        
        // Khởi tạo ChiTietHoaDonDVId với các giá trị mặc định
        ChiTietHoaDonDVId id = new ChiTietHoaDonDVId();
        id.setMaHD(""); // Giá trị tạm thời, sẽ được cập nhật khi lưu
        id.setMaDV(""); // Giá trị tạm thời, sẽ được cập nhật khi lưu
        chiTiet.setId(id);
        
        // Thiết lập mối quan hệ
        chiTiet.setHoaDonDV(hoaDonDV);
        
        // Tạo HoaDonDTO mới
        HoaDonDTO serviceBillForm = new HoaDonDTO();
        
        model.addAttribute("isEditMode", false);
        model.addAttribute("serviceBillForm", serviceBillForm);
        model.addAttribute("hoaDonDV", hoaDonDV);
        model.addAttribute("danhSachTaiKhoan", taiKhoanService.getAllTaiKhoan());
        model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
        model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
        return "admin/service-bill-form";
    }

    @PostMapping("/save")
    public String saveHoaDon(@RequestParam("tenTK") String tenTK,
                            @RequestParam("maDV") String maDV,
                            @RequestParam("soLuong") String soLuongStr,
                            @RequestParam(value = "maUuDai", required = false) String maUuDai,
                            Model model) {
        logger.info("Bắt đầu xử lý lưu hóa đơn dịch vụ");
        logger.info("Dữ liệu từ form - tenTK: {}, maDV: {}, soLuong: {}, maUuDai: {}", 
                   tenTK, maDV, soLuongStr, maUuDai);

        try {
            if (tenTK == null || tenTK.trim().isEmpty()) {
                logger.error("Tên tài khoản trống");
                model.addAttribute("error", "Vui lòng nhập tên tài khoản");
                model.addAttribute("danhSachTaiKhoan", taiKhoanService.getAllTaiKhoan());
                model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
                model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
                return "admin/service-bill-form";
            }
            
            // Kiểm tra tài khoản tồn tại
            List<TaiKhoan> danhSachTaiKhoan = taiKhoanService.getAllTaiKhoan();
            logger.info("Tổng số tài khoản trong hệ thống: {}", danhSachTaiKhoan.size());
            
            // Log danh sách tài khoản để debug
            danhSachTaiKhoan.forEach(tk -> {
                logger.info("Tài khoản trong DB - MaTK: {}, TenTK: {}", tk.getMaTK(), tk.getTenTK());
            });
            
            // Tìm tài khoản theo tên
            Optional<TaiKhoan> taiKhoanOpt = danhSachTaiKhoan.stream()
                .filter(tk -> tk.getTenTK().trim().equalsIgnoreCase(tenTK.trim()))
                .findFirst();
            
            if (taiKhoanOpt.isEmpty()) {
                logger.error("Không tìm thấy tài khoản: {}", tenTK);
                model.addAttribute("error", "Tài khoản không tồn tại trong hệ thống");
                model.addAttribute("danhSachTaiKhoan", danhSachTaiKhoan);
                model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
                model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
                return "admin/service-bill-form";
            }

            // Chuyển đổi số lượng từ String sang Integer
            int soLuong;
            try {
                soLuong = Integer.parseInt(soLuongStr);
                if (soLuong <= 0) {
                    throw new NumberFormatException("Số lượng phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                logger.error("Số lượng không hợp lệ: {}", soLuongStr);
                model.addAttribute("error", "Vui lòng nhập số lượng hợp lệ");
                model.addAttribute("danhSachTaiKhoan", danhSachTaiKhoan);
                model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
                model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
                return "admin/service-bill-form";
            }

            if (maDV == null || maDV.trim().isEmpty()) {
                logger.error("Mã dịch vụ trống");
                model.addAttribute("error", "Vui lòng chọn dịch vụ");
                model.addAttribute("danhSachTaiKhoan", danhSachTaiKhoan);
                model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
                model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
                return "admin/service-bill-form";
            }

            // Tạo request object
            CreateHoaDonRequest request = new CreateHoaDonRequest();
            request.setMaTK(taiKhoanOpt.get().getMaTK());
            request.setMaNV("NV001");
            
            // Tạo chi tiết dịch vụ
            OrderServiceRequest.OrderItemRequest orderItem = new OrderServiceRequest.OrderItemRequest();
            orderItem.setMaDV(maDV);
            orderItem.setSoLuong(soLuong);
            
            // Thêm chi tiết vào request
            List<OrderServiceRequest.OrderItemRequest> items = new ArrayList<>();
            items.add(orderItem);
            request.setItems(items);
            
            // Thêm ưu đãi nếu có
            if (maUuDai != null && !maUuDai.trim().isEmpty()) {
                request.setMaUuDai(maUuDai);
            }

            logger.info("Gửi request tạo hóa đơn: {}", request);
            hoaDonService.createHoaDon(request);
            logger.info("Tạo hóa đơn thành công");
            
            return "redirect:/admin/service-bills";
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hóa đơn: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("danhSachTaiKhoan", taiKhoanService.getAllTaiKhoan());
            model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
            model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
            return "admin/service-bill-form";
        }
    }

    @GetMapping("/edit/{maHD}")
    public String showEditForm(@PathVariable String maHD, Model model) {
        try {
            HoaDonDTO hoaDon = hoaDonService.getHoaDonById(maHD);
            if (hoaDon == null) {
                throw new RuntimeException("Không tìm thấy hóa đơn");
            }
            
            model.addAttribute("isEditMode", true);
            model.addAttribute("serviceBillForm", hoaDon);
            model.addAttribute("danhSachTaiKhoan", taiKhoanService.getAllTaiKhoan());
            model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
            model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDai(Pageable.unpaged()).getContent());
            return "admin/service-bill-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/service-bills";
        }
    }

    @PostMapping("/update")
    public String updateServiceBill(@RequestParam("maHD") String maHD,
                                  @RequestParam("tenTK") String tenTK,
                                  @RequestParam("maDV") String maDV,
                                  @RequestParam("soLuong") String soLuongStr,
                                  @RequestParam(value = "maUuDai", required = false) String maUuDai,
                                  RedirectAttributes redirectAttributes) {
        try {
            logger.info("Bắt đầu cập nhật hóa đơn dịch vụ");
            logger.info("Dữ liệu từ form - maHD: {}, tenTK: {}, maDV: {}, soLuong: {}, maUuDai: {}", 
                       maHD, tenTK, maDV, soLuongStr, maUuDai);

            // Kiểm tra tài khoản tồn tại
            List<TaiKhoan> danhSachTaiKhoan = taiKhoanService.getAllTaiKhoan();
            Optional<TaiKhoan> taiKhoanOpt = danhSachTaiKhoan.stream()
                .filter(tk -> tk.getTenTK().trim().equalsIgnoreCase(tenTK.trim()))
                .findFirst();
            
            if (taiKhoanOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy tài khoản với tên: " + tenTK);
            }

            // Chuyển đổi số lượng từ String sang Integer
            int soLuong;
            try {
                soLuong = Integer.parseInt(soLuongStr);
                if (soLuong <= 0) {
                    throw new NumberFormatException("Số lượng phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Vui lòng nhập số lượng hợp lệ");
            }

            // Tạo request mới
            CreateHoaDonRequest request = new CreateHoaDonRequest();
            request.setMaTK(taiKhoanOpt.get().getMaTK());
            request.setMaNV("NV001"); // Hoặc lấy từ session nếu có
            
            // Tạo chi tiết dịch vụ
            OrderServiceRequest.OrderItemRequest orderItem = new OrderServiceRequest.OrderItemRequest();
            orderItem.setMaDV(maDV);
            orderItem.setSoLuong(soLuong);
            
            // Thêm chi tiết vào request
            List<OrderServiceRequest.OrderItemRequest> items = new ArrayList<>();
            items.add(orderItem);
            request.setItems(items);
            
            // Thêm ưu đãi nếu có
            if (maUuDai != null && !maUuDai.trim().isEmpty()) {
                request.setMaUuDai(maUuDai);
            }

            // Cập nhật hóa đơn
            HoaDonDTO updatedBill = hoaDonService.updateHoaDon(maHD, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hóa đơn thành công!");
            return "redirect:/admin/service-bills";
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hóa đơn: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/service-bills/edit/" + maHD;
        }
    }

    @PostMapping("/delete/{maHD}")
    public String deleteServiceBill(@PathVariable String maHD, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Implement delete functionality in service
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hóa đơn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/service-bills";
    }
} 