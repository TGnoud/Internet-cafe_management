package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nhanvien")
public class NhanVien {

    @Id
    @Column(name = "MaNV", length = 10)
    private String maNV;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "GioiTinh", length = 10)
    private String gioiTinh;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChucVu", nullable = false)
    private ChucVu chucVu;

    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<HoaDonDV> hoaDonDVs;
}