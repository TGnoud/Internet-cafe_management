package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "khachhang")
public class KhachHang {

    @Id
    @Column(name = "MaKH", length = 10)
    private String maKH;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "SoDienThoai", nullable = false, length = 15, unique = true)
    private String soDienThoai;

    @Column(name = "GioiTinh", length = 10)
    private String gioiTinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLoaiKH", nullable = false)
    private LoaiKH loaiKH;

    // Mối quan hệ một-một với TaiKhoan
    // `mappedBy` chỉ ra rằng `TaiKhoan` entity là chủ sở hữu của mối quan hệ này (có cột `MaKH`)
    @OneToOne(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private TaiKhoan taiKhoan;
}