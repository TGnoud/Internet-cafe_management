package com.SpringTest.SpringTest.controller.page;

import com.SpringTest.SpringTest.service.DichVuService;
import com.SpringTest.SpringTest.service.MayTinhService;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.entity.*;
import com.SpringTest.SpringTest.service.*;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid; // Cho validation
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.math.BigDecimal; // Thêm import
import java.time.LocalDate; // Thêm import
import java.time.LocalDateTime; // Thêm import
import java.time.LocalTime; // Thêm import
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;

import com.SpringTest.SpringTest.entity.MayTinh; // Thêm import
import com.SpringTest.SpringTest.service.LoaiMayService; // Thêm import
import com.SpringTest.SpringTest.repository.KhachHangRepository;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.DichVuRepository;
import com.SpringTest.SpringTest.repository.NhanVienRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.SpringTest.SpringTest.exception.BadRequestException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin") // Tất cả các URL trong controller này sẽ bắt đầu bằng /admin
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')") // Bảo vệ toàn bộ controller này
public class AdminPageController {

    @Autowired
    private MayTinhService mayTinhService;

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private TaiKhoanService taiKhoanService; // Giả sử bạn có hàm getAllTaiKhoan() hoặc tương tự

    @Autowired
    private PhienSuDungService phienSuDungService;

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private ChucVuService chucVuService;

    @Autowired
    private LoaiMayService loaiMayService;

    @Autowired
    private LoaiKHService loaiKHService;

    @Autowired
    private UuDaiService uuDaiService;
    private RedirectAttributes redirectAttributes;

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private MayTinhRepository mayTinhRepository;
    @Autowired
    private DichVuRepository dichVuRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;

    /**
        * Hiển thị trang quản lý máy tính với phân trang và tìm kiếm.
     */
    @GetMapping("/manage-computers")
    public String manageComputersPage(Model model,
                                      @RequestParam("page") Optional<Integer> page,
                                      @RequestParam("size") Optional<Integer> size,
                                      @RequestParam("keyword") Optional<String> keyword) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        String searchKeyword = keyword.orElse("");

        // PageRequest.of(page, size) với page index bắt đầu từ 0
        Page<MayTinh> computerPage = mayTinhService.findPaginated(
                PageRequest.of(currentPage - 1, pageSize),
                searchKeyword
        );

        model.addAttribute("computerPage", computerPage);
        model.addAttribute("keyword", searchKeyword);

        int totalPages = computerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("activePage", "computers");

        return "admin/manage-computers.html";
    }

    /**
     * Hiển thị form để thêm mới hoặc chỉnh sửa máy tính.
     */
    @GetMapping({"/computer-form", "/computer-form/{id}"})
    public String computerForm(@PathVariable(required = false) String id, Model model) {
        MayTinh mayTinh = (id != null) ? mayTinhService.getMayTinhById(id) : new MayTinh();
        List<LoaiMay> loaiMayList = loaiMayService.getAllLoaiMay(); // Lấy danh sách loại máy

        model.addAttribute("mayTinh", mayTinh); // Gửi object mayTinh tới form
        model.addAttribute("loaiMayList", loaiMayList); // Gửi danh sách loại máy để đổ vào dropdown

        model.addAttribute("activePage", "computers");

        return "admin/computer-management.html"; // Tên file form của bạn
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        // Lấy thông tin tổng quan
        long soLuongMayTinh = mayTinhService.count();
        long soLuongTaiKhoan = taiKhoanService.count();
        long soLuongDichVu = dichVuService.count();
        long soPhienDangHoatDong = phienSuDungService.countByThoiGianKetThucIsNull();

        model.addAttribute("soLuongMayTinh", soLuongMayTinh);
        model.addAttribute("soLuongTaiKhoan", soLuongTaiKhoan);
        model.addAttribute("soLuongDichVu", soLuongDichVu);
        model.addAttribute("soPhienDangHoatDong", soPhienDangHoatDong);

        return "admin/admin-dashboard";
    }

    // --- Quản Lý Máy Tính ---
    @GetMapping("/computers")
    public String showManageComputersPage(Model model) {
        try {
            List<MayTinh> mayTinhList = mayTinhService.getAllMayTinhList();
            model.addAttribute("mayTinhList", mayTinhList);
            return "admin/manage-computers";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách máy tính");
            return "admin/manage-computers";
        }
    }

    // Form thêm máy tính (GET)
    @GetMapping("/computers/add")
    public String showAddComputerForm(Model model) {
        model.addAttribute("computerForm", new MayTinh()); // Hoặc MayTinhFormDTO
        model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay()); // Cần LoaiMayService
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "computers");
        return "admin/computer-form";
    }
    @PostMapping("/computers/save")
    public String saveComputer(@Valid @ModelAttribute("computerForm") MayTinh computerForm, // Thay MayTinh bằng MayTinhFormDTO
                               BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", false);
            model.addAttribute("activePage", "computers");
            return "admin/computer-form";
        }
        try {
            // Nếu dùng DTO, cần chuyển đổi sang Entity trước khi gọi service.addMayTinh
            // Ví dụ: MayTinh mayTinhEntity = convertToEntity(computerForm);
            // mayTinhService.addMayTinh(mayTinhEntity);
            mayTinhService.addMayTinh(computerForm); // Giả sử service nhận Entity hoặc DTO tương ứng
            redirectAttributes.addFlashAttribute("successMessage", "Thêm máy tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm máy tính: " + e.getMessage());
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", false);
            model.addAttribute("computerForm", computerForm); // Giữ lại dữ liệu đã nhập
            model.addAttribute("activePage", "computers");
            return "admin/computer-form";
        }
        return "redirect:/admin/computers";
    }

    // Form sửa máy tính (GET)
    @GetMapping("/computers/edit/{maMay}")
    public String showEditComputerForm(@PathVariable String maMay, Model model) {
        try {
            MayTinh mayTinh = mayTinhService.getMayTinhById(maMay);
            // MayTinhFormDTO formDTO = convertToFormDTO(mayTinh); // Nếu dùng DTO
            model.addAttribute("computerForm", mayTinh); // Hoặc formDTO
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "computers");
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", "Không tìm thấy máy tính với mã: " + maMay);
            return "redirect:/admin/computers"; // Hoặc một trang lỗi riêng
        }
        return "admin/computer-form";
    }
    @PostMapping("/computers/update")
    public String updateComputer(@Valid @ModelAttribute("computerForm") MayTinh computerForm,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "computers");
            return "admin/computer-form";
        }
        try {
            MayTinh existingComputer = mayTinhService.getMayTinhById(computerForm.getMaMay());
            if (existingComputer == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy máy tính với mã: " + computerForm.getMaMay());
                return "redirect:/admin/computers";
            }
            
            // Cập nhật thông tin máy tính
            existingComputer.setTenMay(computerForm.getTenMay());
            existingComputer.setTrangThai(computerForm.getTrangThai());
            existingComputer.setLoaiMay(computerForm.getLoaiMay());
            
            mayTinhService.updateMayTinh(existingComputer.getMaMay(), existingComputer);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật máy tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật máy tính: " + e.getMessage());
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
            model.addAttribute("computerForm", computerForm);
            model.addAttribute("activePage", "computers");
            return "admin/computer-form";
        }
        return "redirect:/admin/computers";
    }

    // --- Quản Lý Dịch Vụ ---
    @GetMapping("/services")
    public String showManageServicesPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        // Page<DichVuDTO> dichVuPage = dichVuService.getAllDichVuPageable(pageable); // Cần service hỗ trợ
        // model.addAttribute("dichVuPage", dichVuPage);
        model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
        model.addAttribute("activePage", "services");
        return "admin/manage-services";
    }

    @GetMapping("/services/add")
    public String showAddServiceForm(Model model) {
        model.addAttribute("serviceForm", new DichVuDTO()); // DTO này đã có và phù hợp
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "services");
        return "admin/service-form";
    }

    @PostMapping("/services/save")
    public String saveService(@Valid @ModelAttribute("serviceForm") DichVuDTO serviceForm,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", false);
            model.addAttribute("activePage", "services");
            return "admin/service-form";
        }
        try {
            dichVuService.addDichVu(serviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm dịch vụ thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm dịch vụ: " + e.getMessage());
            model.addAttribute("isEditMode", false);
            model.addAttribute("serviceForm", serviceForm);
            model.addAttribute("activePage", "services");
            return "admin/service-form";
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/services/edit/{maDV}")
    public String showEditServiceForm(@PathVariable String maDV, Model model) {
        try {
            model.addAttribute("serviceForm", dichVuService.getDichVuByMaDV(maDV));
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "services");
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", "Không tìm thấy dịch vụ: " + maDV);
            return "redirect:/admin/services";
        }
        return "admin/service-form";
    }

    @PostMapping("/services/update")
    public String updateService(@Valid @ModelAttribute("serviceForm") DichVuDTO serviceForm,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "services");
            return "admin/service-form";
        }
        try {
            dichVuService.updateDichVu(serviceForm.getMaDV(), serviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật dịch vụ thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật dịch vụ: " + e.getMessage());
            model.addAttribute("isEditMode", true);
            model.addAttribute("serviceForm", serviceForm);
            model.addAttribute("activePage", "services");
            return "admin/service-form";
        }
        return "redirect:/admin/services";
    }

    @DeleteMapping("/services/delete/{maDV}")
    @ResponseBody
    public ResponseEntity<String> deleteService(@PathVariable String maDV) {
        try {
            dichVuService.deleteDichVu(maDV);
            return ResponseEntity.ok().body("Dịch vụ đã được xóa thành công.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa dịch vụ: " + e.getMessage());
        }
    }

    @DeleteMapping("/computers/delete/{maMay}")
    @ResponseBody
    public ResponseEntity<String> deleteComputer(@PathVariable String maMay) {
        try {
            mayTinhService.deleteMayTinh(maMay);
            return ResponseEntity.ok("Máy tính đã được xóa thành công");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa máy tính: " + (e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định."));
        }
    }

    // --- Quản Lý Tài Khoản Khách Hàng ---
    @GetMapping("/accounts")
    public String showManageAccountsPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        try {
            System.out.println("Đang tải danh sách tài khoản...");
            Page<TaiKhoanInfoResponse> taiKhoanPage = taiKhoanService.getAllKhachHangTaiKhoanPageable(pageable);
            
            System.out.println("Số tài khoản: " + taiKhoanPage.getTotalElements());
            System.out.println("Số trang: " + taiKhoanPage.getTotalPages());
            
            model.addAttribute("accounts", taiKhoanPage.getContent());
            model.addAttribute("currentPage", taiKhoanPage.getNumber());
            model.addAttribute("totalPages", taiKhoanPage.getTotalPages());
            model.addAttribute("activePage", "accounts");
            
            return "admin/manage-accounts";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách tài khoản: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách tài khoản: " + e.getMessage());
            return "admin/manage-accounts";
        }
    }

    @GetMapping("/accounts/add")
    public String showAddAccountForm(Model model) {
        model.addAttribute("form", new CreateTaiKhoanRequest());
        model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "accounts");
        return "admin/account-form";
    }

    @PostMapping("/accounts/save")
    public String saveAccount(@Valid @ModelAttribute("form") CreateTaiKhoanRequest form,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", false);
            model.addAttribute("form", form);
            model.addAttribute("activePage", "accounts");
            return "admin/account-form";
        }

        try {
            taiKhoanService.createTaiKhoanKhachHang(form);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản thành công!");
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm tài khoản: " + e.getMessage());
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", false);
            model.addAttribute("form", form);
            model.addAttribute("activePage", "accounts");
            return "admin/account-form";
        }
    }

    // Sửa tài khoản khách hàng có thể bao gồm sửa thông tin KhachHang và Loại KH
    // Việc thay đổi mật khẩu hoặc các thông tin nhạy cảm khác cần cân nhắc kỹ
    @GetMapping("/accounts/edit/{maTK}")
    public String showEditAccountForm(@PathVariable String maTK, Model model) {
        try {
            TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(maTK);
            CreateTaiKhoanRequest form = new CreateTaiKhoanRequest();
            TaiKhoan tk = taiKhoanService.findEntityByMaTK(maTK);
            if (tk != null) {
                form.setMaTK(tk.getMaTK());
                form.setTenTK(tk.getTenTK());
                form.setMatKhau(tk.getMatKhau());
                form.setSoTienConLai(tk.getSoTienConLai());
                if (tk.getKhachHang() != null) {
                    form.setMaKH(tk.getKhachHang().getMaKH());
                    form.setHoTenKH(tk.getKhachHang().getHoTen());
                    form.setSoDienThoaiKH(tk.getKhachHang().getSoDienThoai());
                    form.setGioiTinhKH(tk.getKhachHang().getGioiTinh());
                    if (tk.getKhachHang().getLoaiKH() != null) {
                        form.setMaLoaiKH(tk.getKhachHang().getLoaiKH().getMaLoaiKH());
                    }
                }
            }

            model.addAttribute("form", form);
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "accounts");
            return "admin/account-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không tìm thấy tài khoản: " + maTK);
            return "redirect:/admin/accounts";
        }
    }

    @PostMapping("/accounts/update")
    public String updateAccount(@Valid @ModelAttribute("form") CreateTaiKhoanRequest form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "accounts");
            model.addAttribute("form", form);
            return "admin/account-form";
        }
        try {
            taiKhoanService.updateTaiKhoanKhachHang(form.getMaTK(), form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật tài khoản: " + e.getMessage());
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", true);
            model.addAttribute("form", form);
            model.addAttribute("activePage", "accounts");
            return "admin/account-form";
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/accounts/deposit/{maTK}")
    public String showDepositForm(@PathVariable String maTK, Model model, RedirectAttributes redirectAttributes) {
        try {
            TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(maTK);
            model.addAttribute("taiKhoanInfo", tkInfo);
            model.addAttribute("napTienRequest", new NapTienRequest(maTK, null));
            model.addAttribute("activePage", "accounts");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản: " + maTK);
            return "redirect:/admin/accounts";
        }
        return "admin/deposit-form"; // Tạo trang deposit-form.html
    }

    @PostMapping("/accounts/deposit/save")
    public String saveDeposit(@Valid @ModelAttribute("napTienRequest") NapTienRequest napTienRequest,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            try { // Cần load lại thông tin tài khoản để hiển thị form
                TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(napTienRequest.getMaTK());
                model.addAttribute("taiKhoanInfo", tkInfo);
            } catch (ResourceNotFoundException e) { /* Bỏ qua, lỗi binding chính hơn */ }
            model.addAttribute("activePage", "accounts");
            return "admin/deposit-form";
        }
        try {
            taiKhoanService.napTien(napTienRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Nạp tiền thành công cho tài khoản " + napTienRequest.getMaTK());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nạp tiền: " + e.getMessage());
            // Quay lại trang nạp tiền hoặc trang chi tiết tài khoản
            return "redirect:/admin/accounts/deposit/" + napTienRequest.getMaTK(); // Hoặc /admin/accounts
        }
        return "redirect:/admin/accounts/edit/" + napTienRequest.getMaTK(); // Hoặc /admin/accounts
    }

    // --- Xem Phiên Sử Dụng ---
    @GetMapping("/sessions")
    public String viewSessions(Model model, 
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PhienSuDung> phienPage = phienSuDungService.getAllPhienSuDung(pageable);
        
        model.addAttribute("phienList", phienPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", phienPage.getTotalPages());
        
        // Lấy danh sách máy tính và tài khoản khả dụng
        List<MayTinh> availableComputers = mayTinhService.getAvailableComputers();
        List<TaiKhoan> availableAccounts = taiKhoanService.getAllTaiKhoan();
        
        model.addAttribute("availableComputers", availableComputers);
        model.addAttribute("availableAccounts", availableAccounts);
        
        return "admin/view-sessions";
    }

    @PostMapping("/sessions")
    @ResponseBody
    public ResponseEntity<?> createSession(@RequestBody Map<String, String> request) {
        try {
            String maMay = request.get("maMay");
            String maTK = request.get("maTK");
            
            PhienSuDung phien = phienSuDungService.createPhienSuDung(maMay, maTK);
            return ResponseEntity.ok(phien);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/sessions/{maPhien}/end")
    @ResponseBody
    public ResponseEntity<?> endSession(@PathVariable String maPhien) {
        try {
            phienSuDungService.endPhienSuDung(maPhien);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/sessions/add")
    public String showAddSessionForm(Model model) {
        model.addAttribute("isEditMode", false);
        model.addAttribute("sessionForm", new PhienSuDung());
        model.addAttribute("availableComputers", mayTinhService.getAvailableComputers());
        return "admin/session-form";
    }

    @GetMapping("/sessions/edit/{maPhien}")
    public String showEditSessionForm(@PathVariable Integer maPhien, Model model) {
        PhienSuDung phien = phienSuDungService.findById(maPhien);
        if (phien == null) {
            throw new RuntimeException("Không tìm thấy phiên sử dụng");
        }
        
        model.addAttribute("isEditMode", true);
        model.addAttribute("sessionForm", phien);
        model.addAttribute("availableComputers", mayTinhService.getAllMayTinh());
        return "admin/session-form";
    }

    @PostMapping("/sessions/save")
    public String saveSession(@ModelAttribute("sessionForm") PhienSuDung phien,
                            @RequestParam("tenTK") String tenTK,
                            RedirectAttributes redirectAttributes) {
        try {
            // Tìm tài khoản theo tên
            TaiKhoan taiKhoan = taiKhoanService.findByTenTK(tenTK);
            if (taiKhoan == null) {
                throw new RuntimeException("Không tìm thấy tài khoản với tên: " + tenTK);
            }

            // Tạo phiên mới không cần chỉ định mã phiên
            phienSuDungService.createPhienSuDung(phien.getMayTinh().getMaMay(), taiKhoan.getMaTK());
            redirectAttributes.addFlashAttribute("successMessage", "Phiên sử dụng đã được tạo thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sessions/add";
        }
        return "redirect:/admin/sessions";
    }

    @PostMapping("/sessions/update")
    public String updateSession(@ModelAttribute("sessionForm") PhienSuDung phien,
                              @RequestParam("tenTK") String tenTK,
                              RedirectAttributes redirectAttributes) {
        try {
            PhienSuDung existingPhien = phienSuDungService.findById(phien.getMaPhien());
            if (existingPhien == null) {
                throw new RuntimeException("Không tìm thấy phiên sử dụng");
            }

            // Tìm tài khoản theo tên
            TaiKhoan taiKhoan = taiKhoanService.findByTenTK(tenTK);
            if (taiKhoan == null) {
                throw new RuntimeException("Không tìm thấy tài khoản với tên: " + tenTK);
            }

            // Cập nhật thông tin
            existingPhien.setMayTinh(phien.getMayTinh());
            existingPhien.setTaiKhoan(taiKhoan);
            existingPhien.setThoiGianBatDau(phien.getThoiGianBatDau());
            existingPhien.setThoiGianKetThuc(phien.getThoiGianKetThuc());

            phienSuDungService.save(existingPhien);
            redirectAttributes.addFlashAttribute("successMessage", "Phiên sử dụng đã được cập nhật thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sessions/edit/" + phien.getMaPhien();
        }
        return "redirect:/admin/sessions";
    }

    @DeleteMapping("/sessions/{maPhien}")
    @ResponseBody
    public ResponseEntity<String> deleteSession(@PathVariable Integer maPhien) {
        try {
            phienSuDungService.deletePhienSuDung(maPhien);
            return ResponseEntity.ok("Phiên sử dụng đã được xóa thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    // --- Quản Lý Nhân Viên ---
    @GetMapping("/employees")
    public String showManageEmployeesPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<NhanVien> employeePage = nhanVienService.getAllNhanVien(pageable);
            if (employeePage == null) {
                model.addAttribute("error", "Không thể lấy danh sách nhân viên");
                return "admin/manage-employees";
            }
            model.addAttribute("nhanVienList", employeePage.getContent());
            model.addAttribute("currentPage", employeePage.getNumber());
            model.addAttribute("totalPages", employeePage.getTotalPages());
            model.addAttribute("activePage", "employees");
            return "admin/manage-employees";
        } catch (Exception e) {
            e.printStackTrace(); // In stack trace để debug
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách nhân viên: " + e.getMessage());
            return "admin/manage-employees";
        }
    }

    @GetMapping("/employees/add")
    public String showAddEmployeeForm(Model model) {
        try {
            model.addAttribute("employeeForm", new NhanVien());
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", false);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/employees";
        }
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@Valid @ModelAttribute("employeeForm") NhanVien employeeForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", false);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        }

        try {
            // Tạo đối tượng ChucVu từ mã chức vụ
            ChucVu chucVu = chucVuService.findById(employeeForm.getChucVu().getMaChucVu());
            employeeForm.setChucVu(chucVu);
            
            nhanVienService.saveNhanVien(employeeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công!");
            return "redirect:/admin/employees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm nhân viên: " + e.getMessage());
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", false);
            model.addAttribute("employeeForm", employeeForm);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        }
    }

    @GetMapping("/employees/edit/{maNV}")
    public String showEditEmployeeForm(@PathVariable String maNV, Model model, RedirectAttributes redirectAttributes) {
        try {
            NhanVien nv = nhanVienService.getNhanVienById(maNV);
            if (nv == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên: " + maNV);
                return "redirect:/admin/employees";
            }
            
            // Đảm bảo chức vụ được load đầy đủ
            if (nv.getChucVu() != null) {
                ChucVu chucVu = chucVuService.findById(nv.getChucVu().getMaChucVu());
                nv.setChucVu(chucVu);
            }
            
            model.addAttribute("employeeForm", nv);
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin nhân viên: " + e.getMessage());
            return "redirect:/admin/employees";
        }
    }

    @PostMapping("/employees/update")
    public String updateEmployee(@Valid @ModelAttribute("employeeForm") NhanVien employeeForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        }

        try {
            // Lấy thông tin nhân viên hiện tại
            NhanVien existingNhanVien = nhanVienService.getNhanVienById(employeeForm.getMaNV());
            if (existingNhanVien == null) {
                model.addAttribute("errorMessage", "Không tìm thấy nhân viên với mã: " + employeeForm.getMaNV());
                model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
                model.addAttribute("isEditMode", true);
                model.addAttribute("activePage", "employees");
                return "admin/employee-form";
            }
            
            // Cập nhật các thông tin cơ bản
            existingNhanVien.setHoTen(employeeForm.getHoTen());
            existingNhanVien.setSoDienThoai(employeeForm.getSoDienThoai());
            existingNhanVien.setGioiTinh(employeeForm.getGioiTinh());
            existingNhanVien.setNgaySinh(employeeForm.getNgaySinh());
            
            // Cập nhật chức vụ
            if (employeeForm.getChucVu() != null) {
                ChucVu chucVu = chucVuService.findById(employeeForm.getChucVu().getMaChucVu());
                if (chucVu == null) {
                    model.addAttribute("errorMessage", "Không tìm thấy chức vụ với mã: " + employeeForm.getChucVu().getMaChucVu());
                    model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
                    model.addAttribute("isEditMode", true);
                    model.addAttribute("activePage", "employees");
                    return "admin/employee-form";
                }
                existingNhanVien.setChucVu(chucVu);
            }
            
            nhanVienService.updateNhanVien(existingNhanVien.getMaNV(), existingNhanVien);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
            return "redirect:/admin/employees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
            model.addAttribute("employeeForm", employeeForm);
            model.addAttribute("activePage", "employees");
            return "admin/employee-form";
        }
    }

    @DeleteMapping("/employees/delete/{maNV}")
    @ResponseBody
    public ResponseEntity<String> deleteEmployee(@PathVariable String maNV) {
        try {
            nhanVienService.deleteNhanVien(maNV);
            return ResponseEntity.ok("Nhân viên đã được xóa thành công");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa nhân viên: " + (e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định."));
        }
    }

    // --- Quản Lý Ưu Đãi ---
    @GetMapping("/promotions")
    public String showManagePromotionsPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<UuDai> promotionPage = uuDaiService.getAllUuDai(pageable);
            model.addAttribute("promotionPage", promotionPage);
            model.addAttribute("activePage", "promotions");
            return "admin/manage-promotions";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách ưu đãi: " + e.getMessage());
            return "admin/manage-promotions";
        }
    }

    @GetMapping("/promotions/add")
    public String showAddPromotionForm(Model model) {
        model.addAttribute("promotionForm", new UuDai()); // Hoặc UuDaiFormDTO
        model.addAttribute("isEditMode", false);
        model.addAttribute("activePage", "promotions");
        return "admin/promotion-form";
    }

    @PostMapping("/promotions/save")
    public String savePromotion(@Valid @ModelAttribute("promotionForm") UuDai promotionForm, // Thay bằng DTO
                                BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", false);
            model.addAttribute("activePage", "promotions");
            return "admin/promotion-form";
        }
        try {
            uuDaiService.saveUuDai(promotionForm); // Service nên nhận DTO
            redirectAttributes.addFlashAttribute("successMessage", "Thêm ưu đãi thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm ưu đãi: " + e.getMessage());
            model.addAttribute("isEditMode", false);
            model.addAttribute("promotionForm", promotionForm);
            model.addAttribute("activePage", "promotions");
            return "admin/promotion-form";
        }
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/edit/{maUuDai}")
    public String showEditPromotionForm(@PathVariable String maUuDai, Model model, RedirectAttributes redirectAttributes) {
        try {
            UuDai ud = uuDaiService.getUuDaiById(maUuDai);
            model.addAttribute("promotionForm", ud); // Hoặc DTO
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "promotions");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ưu đãi: " + maUuDai);
            return "redirect:/admin/promotions";
        }
        return "admin/promotion-form";
    }

    @PostMapping("/promotions/update")
    public String updatePromotion(@Valid @ModelAttribute("promotionForm") UuDai promotionForm, // Thay bằng DTO
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", true);
            model.addAttribute("activePage", "promotions");
            return "admin/promotion-form";
        }
        try {
            uuDaiService.updateUuDai(promotionForm.getMaUuDai(), promotionForm); // Service nên nhận DTO
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ưu đãi thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật ưu đãi: " + e.getMessage());
            model.addAttribute("isEditMode", true);
            model.addAttribute("promotionForm", promotionForm);
            model.addAttribute("activePage", "promotions");
            return "admin/promotion-form";
        }
        return "redirect:/admin/promotions";
    }

    @DeleteMapping("/promotions/delete/{maUuDai}")
    @ResponseBody
    public ResponseEntity<String> deletePromotion(@PathVariable String maUuDai) {
        try {
            uuDaiService.deleteUuDai(maUuDai);
            return ResponseEntity.ok().body("Ưu đãi đã được xóa thành công.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa ưu đãi: " + e.getMessage());
        }
    }

    @DeleteMapping("/accounts/delete/{maTK}")
    public ResponseEntity<String> deleteAccount(@PathVariable String maTK) {
        try {
            taiKhoanService.deleteById(maTK);
            return ResponseEntity.ok("Tài khoản đã được xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa tài khoản: " + e.getMessage());
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Bạn sẽ cần tạo các file HTML tương ứng cho các trang trên
    // (manage-computers.html, computer-form.html, manage-services.html, etc.)
    // bên trong thư mục src/main/resources/templates/admin/
}