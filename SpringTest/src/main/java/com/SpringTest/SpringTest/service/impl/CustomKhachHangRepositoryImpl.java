package com.SpringTest.SpringTest.service.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

@Repository // Hoặc @Component nếu bạn muốn custom repository
public class CustomKhachHangRepositoryImpl { // Hoặc tên khác phù hợp

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void themKhachHangProcedure(String hoTen, String email, String soDienThoai,
                                       String tenTaiKhoan, String matKhau, Integer maLoaiKH) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("ThemKhachHang");

        // Đăng ký các tham số IN
        query.registerStoredProcedureParameter("p_HoTen", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_Email", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_SoDienThoai", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_TenTaiKhoan", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_MatKhau", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_MaLoaiKH", Integer.class, ParameterMode.IN);

        // Set giá trị cho các tham số
        query.setParameter("p_HoTen", hoTen);
        query.setParameter("p_Email", email);
        query.setParameter("p_SoDienThoai", soDienThoai);
        query.setParameter("p_TenTaiKhoan", tenTaiKhoan);
        query.setParameter("p_MatKhau", matKhau); // Nên mã hóa mật khẩu trước khi truyền vào
        query.setParameter("p_MaLoaiKH", maLoaiKH);

        query.execute();
    }
}