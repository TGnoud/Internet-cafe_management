package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.HoaDonDTO;
import com.SpringTest.SpringTest.dto.request.CreateHoaDonRequest;
import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.request.MayTinhStatusRequest;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.HoaDonDV;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
@RestController
@RequestMapping("/api/employee")
// @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
public class EmployeeController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private MayTinhService mayTinhService; // Cần tạo

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private PhienSuDungService phienSuDungService; // Đã có từ trước

    // --- Quản lý Dịch Vụ ---
    @GetMapping("/sessions/active")
    public ResponseEntity<List<PhienSuDung>> getActiveSessions() {
        return ResponseEntity.ok(phienSuDungService.getPhienSuDungByTaiKhoan(null));
    }

    @GetMapping("/sessions/machine/{maMay}")
    public ResponseEntity<List<PhienSuDung>> getSessionHistoryByMachine(
            @PathVariable String maMay,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(phienSuDungService.getPhienSuDungByTaiKhoan(maMay));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<PhienSuDung>> getAllSessionHistory(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(phienSuDungService.getPhienSuDungByTaiKhoan(null));
    }

    @PostMapping("/services")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<DichVuDTO> addDichVu(@RequestBody DichVuDTO dichVuDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dichVuService.addDichVu(dichVuDTO));
    }

    @PutMapping("/services/{maDV}")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<DichVuDTO> updateDichVu(@PathVariable String maDV, @RequestBody DichVuDTO dichVuDTO) {
        return ResponseEntity.ok(dichVuService.updateDichVu(maDV, dichVuDTO));
    }

    @GetMapping("/services")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'CUSTOMER')") // Customer có thể xem
    public ResponseEntity<List<DichVuDTO>> getAllDichVu() {
        return ResponseEntity.ok(dichVuService.getAllDichVu());
    }

    @GetMapping("/services/{maDV}")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<DichVuDTO> getDichVuByMaDV(@PathVariable String maDV) {
        return ResponseEntity.ok(dichVuService.getDichVuByMaDV(maDV));
    }
    @DeleteMapping("/services/{maDV}")
    // @PreAuthorize("hasRole('MANAGER')") // Chỉ quản lý mới được xóa hẳn, nhân viên có thể chỉ đổi trạng thái
    // Hoặc @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')") nếu nhân viên cũng có quyền này
    public ResponseEntity<String> deleteDichVu(@PathVariable String maDV) {
        dichVuService.deleteDichVu(maDV);
        return ResponseEntity.ok("Dịch vụ " + maDV + " đã được xóa thành công.");
    }

    // --- Lập Hóa Đơn Dịch Vụ ---
    @PostMapping("/invoices")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<HoaDonDTO> createInvoice(@RequestBody CreateHoaDonRequest request) {
        // Cần lấy MaNV của nhân viên đang đăng nhập từ Principal
        // String maNVCurrentUser = authentication.getName(); // Hoặc cách khác
        // request.setMaNV(maNVCurrentUser); // Gán vào request nếu không tự nhập

        if (request.getMaNV() == null || request.getMaNV().isEmpty()){
            throw new BadRequestException("Mã nhân viên không được để trống khi tạo hóa đơn.");
        }

        HoaDonDTO hoaDonDTO = hoaDonService.createHoaDon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(hoaDonDTO);
    }

    @GetMapping("/invoices/{maHD}")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<HoaDonDTO> getInvoiceById(@PathVariable String maHD) {
        return ResponseEntity.ok(hoaDonService.getHoaDonById(maHD));
    }

    // --- Theo dõi phiên sử dụng ---
    // Cần mở rộng PhienSuDungService để có các hàm lấy danh sách phiên
    /*
    @GetMapping("/sessions/active")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<List<PhienSuDungInfoResponse>> getActiveSessions() {
        // return ResponseEntity.ok(phienSuDungService.getActiveSessions());
        return ResponseEntity.ok(new ArrayList<>()); // Placeholder
    }

    @GetMapping("/sessions/history/machine/{maMay}")
    // @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<List<PhienSuDungInfoResponse>> getSessionHistoryByMachine(@PathVariable String maMay) {
        // return ResponseEntity.ok(phienSuDungService.getSessionHistoryByMachine(maMay));
         return ResponseEntity.ok(new ArrayList<>()); // Placeholder
    }
    */
    @PostMapping("/service-bills")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<HoaDonDV> createServiceBill(@RequestBody CreateHoaDonRequest request) {
        HoaDonDV newBill = hoaDonService.createHoaDonDV(request); // Cần tạo method này trong service
        return ResponseEntity.status(HttpStatus.CREATED).body(newBill);
    }
    @PostMapping("/customers/create")
    public ResponseEntity<TaiKhoanInfoResponse> createCustomerAccount(@RequestBody CreateTaiKhoanRequest request) {
        return ResponseEntity.ok(taiKhoanService.createTaiKhoanKhachHang(request));
    }

    @PostMapping("/customers/deposit")
    public ResponseEntity<TaiKhoanInfoResponse> depositToAccount(@RequestBody NapTienRequest request) {
        return ResponseEntity.ok(taiKhoanService.napTien(request));
    }

    @PutMapping("/computers/status/{maMay}")
    public ResponseEntity<MayTinh> updateMayTinhStatus(@PathVariable String maMay, @RequestBody MayTinhStatusRequest request) {
        MayTinh updatedMayTinh = mayTinhService.updateTrangThaiMay(maMay, request.getTrangThaiMoi());
        return ResponseEntity.ok(updatedMayTinh);
    }

    @PostMapping("/sessions/{maPhien}/end")
    public ResponseEntity<PhienSuDung> endSession(
            @PathVariable Integer maPhien,
            @RequestParam(defaultValue = "false") boolean apDungUuDai) {
        return ResponseEntity.ok(phienSuDungService.ketThucPhienSuDung(maPhien));
    }
    @PostMapping("/service-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<HoaDonDV> createServiceOrder(@RequestBody CreateHoaDonRequest request) {
        // Gán mã nhân viên đang đăng nhập vào request nếu cần
        // String maNV = SecurityContextHolder.getContext().getAuthentication().getName();
        // request.setMaNV(maNV);

        HoaDonDV createdBill = hoaDonService.createHoaDonDV(request);
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }
}