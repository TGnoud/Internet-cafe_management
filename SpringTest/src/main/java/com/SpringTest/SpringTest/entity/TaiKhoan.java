package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "taikhoan")
public class TaiKhoan {

    @Id
    @Column(name = "MaTK", length = 10)
    private String maTK;

    @Column(name = "TenTK", nullable = false, unique = true, length = 45)
    private String tenTK;

    @Column(name = "MatKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "SoTienConLai", nullable = false, precision = 10, scale = 2)
    private BigDecimal soTienConLai;

    // Mối quan hệ một-một với KhachHang
    // `JoinColumn` chỉ định cột khóa ngoại trong bảng TaiKhoan
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false, unique = true)
    private KhachHang khachHang;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PhienSuDung> phienSuDungs;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<HoaDonDV> hoaDonDVs;
}