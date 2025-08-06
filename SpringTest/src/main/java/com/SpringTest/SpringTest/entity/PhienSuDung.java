package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phiensudung")
public class PhienSuDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhien") // INT UNSIGNED NOT NULL AUTO_INCREMENT
    private Integer maPhien; // Hoặc Long nếu giá trị có thể lớn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaMay", nullable = false)
    private MayTinh mayTinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTK", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "ThoiGianBatDau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "ThoiGianKetThuc")
    private LocalDateTime thoiGianKetThuc;
}