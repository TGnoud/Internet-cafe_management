package com.SpringTest.SpringTest.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String tenTK; // TenTaiKhoan
    private String matKhau;
    private String maMay; // Mã máy khách hàng muốn đăng nhập
}