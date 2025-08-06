package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
// Bỏ import java.math.BigDecimal; vì không có trường nào dùng trong DDL này (trừ khi bạn muốn thêm Thành Tiền)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ct_hoadondv")
public class ChiTietHoaDonDV {

    @EmbeddedId
    private ChiTietHoaDonDVId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maHD") // maps maHD attribute of embedded id
    @JoinColumn(name = "MaHD")
    private HoaDonDV hoaDonDV;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maDV") // maps maDV attribute of embedded id
    @JoinColumn(name = "MaDV")
    private DichVu dichVu;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    public void setDonGia(BigDecimal donGia) {

    }

    // Nếu muốn tính toán và lưu Thành Tiền, bạn có thể thêm:
    // @Column(name = "ThanhTien", nullable = false, precision = 10, scale = 2)
    // private BigDecimal thanhTien;
}