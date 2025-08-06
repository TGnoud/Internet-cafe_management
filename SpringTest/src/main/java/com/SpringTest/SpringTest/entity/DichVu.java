package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dichvu")
public class DichVu {

    @Id
    @Column(name = "MaDV", length = 10)
    private String maDV;

    @Column(name = "TenDV", nullable = false, length = 100)
    private String tenDV;

    @Column(name = "DonGia", nullable = false, precision = 10, scale = 2)
    private BigDecimal donGia;

    @Column(name = "TrangThaiDichVu", length = 45) // Ví dụ: Còn hàng, Hết hàng
    private String trangThaiDichVu;

    @OneToMany(mappedBy = "dichVu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChiTietHoaDonDV> chiTietHoaDonDVs;
}