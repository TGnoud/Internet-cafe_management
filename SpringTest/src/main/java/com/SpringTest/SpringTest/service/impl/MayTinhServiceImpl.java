package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.LoaiMay;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.LoaiMayRepository;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.service.MayTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager; // Thêm import này
import jakarta.persistence.ParameterMode; // Thêm import này
import jakarta.persistence.PersistenceContext; // Thêm import này
import jakarta.persistence.StoredProcedureQuery; // Thêm import này
import java.util.Arrays;
import java.util.List;

@Service
public class MayTinhServiceImpl implements MayTinhService {

    @Autowired
    private MayTinhRepository mayTinhRepository;
    @Autowired
    private PhienSuDungRepository phienSuDungRepository;
    @Autowired
    private LoaiMayRepository loaiMayRepository;

    @PersistenceContext // Injecct EntityManager
    private EntityManager entityManager;
    private static final List<String> VALID_STATUSES = Arrays.asList("Khả dụng", "Bảo trì");

    public String getActualMachineStatusFromDb(String maMay) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("KiemTraTrangThaiMay");
        query.registerStoredProcedureParameter("p_MaMay", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_TrangThai", String.class, ParameterMode.OUT); // Tham số OUT

        query.setParameter("p_MaMay", maMay);
        query.execute();

        return (String) query.getOutputParameterValue("p_TrangThai");
    }
    @Override
    public List<MayTinh> getAllMayTinh() {
        return mayTinhRepository.findAll();
    }

    @Override
    public List<MayTinh> getAllMayTinhList() {
        return mayTinhRepository.findAll();
    }

    @Override
    public MayTinh getMayTinhById(String maMay) {
        return mayTinhRepository.findById(maMay).orElse(null);
    }

    @Override
    @Transactional
    public MayTinh updateTrangThaiMay(String maMay, String trangThaiMoi) {
        MayTinh mayTinh = getMayTinhById(maMay);
        if (mayTinh != null) {
            mayTinh.setTrangThai(trangThaiMoi);
            return mayTinhRepository.save(mayTinh);
        }
        return null;
    }

    @Override
    @Transactional
    public MayTinh addMayTinh(MayTinh mayTinh) { // DTO sẽ tốt hơn
        if(mayTinhRepository.existsById(mayTinh.getMaMay())){
            throw new BadRequestException("Mã máy " + mayTinh.getMaMay() + " đã tồn tại.");
        }
        // Kiểm tra LoaiMay có tồn tại không
        LoaiMay loaiMay = loaiMayRepository.findById(mayTinh.getLoaiMay().getMaLoaiMay())
                .orElseThrow(() -> new ResourceNotFoundException("Loại máy " + mayTinh.getLoaiMay().getMaLoaiMay() + " không tồn tại."));
        mayTinh.setLoaiMay(loaiMay); // Đảm bảo object LoaiMay được managed
        if (mayTinh.getTrangThai() == null || mayTinh.getTrangThai().isEmpty()) {
            mayTinh.setTrangThai("Khả dụng"); // Mặc định
        }
        return mayTinhRepository.save(mayTinh);
    }

    @Override
    @Transactional
    public void deleteMayTinh(String maMay) {
        MayTinh mayTinh = getMayTinhById(maMay);
        if (mayTinh == null) {
            throw new ResourceNotFoundException("Không tìm thấy máy tính với mã: " + maMay);
        }
        // Kiểm tra ràng buộc, ví dụ: không xóa máy đang có phiên hoạt động
        if (!phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isEmpty()) {
            throw new BadRequestException("Không thể xóa máy " + maMay + " khi đang có phiên sử dụng.");
        }
        try {
            mayTinhRepository.delete(mayTinh);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Lỗi khi xóa máy tính: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public MayTinh updateMayTinh(String maMay, MayTinh mayTinhDetails) { // DTO sẽ tốt hơn
        MayTinh existingMayTinh = getMayTinhById(maMay);
        existingMayTinh.setTenMay(mayTinhDetails.getTenMay());
        // Cập nhật LoaiMay
        if (mayTinhDetails.getLoaiMay() != null && mayTinhDetails.getLoaiMay().getMaLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(mayTinhDetails.getLoaiMay().getMaLoaiMay())
                    .orElseThrow(() -> new ResourceNotFoundException("Loại máy " + mayTinhDetails.getLoaiMay().getMaLoaiMay() + " không tồn tại."));
            existingMayTinh.setLoaiMay(loaiMay);
        }
        // Cẩn thận khi cập nhật trạng thái ở đây, nên có API riêng như updateTrangThaiMay
        // existingMayTinh.setTrangThai(mayTinhDetails.getTrangThai());
        return mayTinhRepository.save(existingMayTinh);
    }

    @Override
    public long countAllMayTinh() {
        return 0;
    }

    @Override
    public long countMayTinhByTrangThai(String đangSửDụng) {
        return 0;
    }

    @Override
    public Page<MayTinh> findPaginated(PageRequest of, String searchKeyword) {
        return null;
    }

    @Override
    public long count() {
        return mayTinhRepository.count();
    }

    @Override
    public List<MayTinh> getAvailableComputers() {
        return mayTinhRepository.findByTrangThai("Khả dụng");
    }
}