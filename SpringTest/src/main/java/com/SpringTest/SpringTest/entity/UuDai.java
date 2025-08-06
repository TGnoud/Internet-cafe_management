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
@Table(name = "uudai")
public class UuDai {

    @Id
    @Column(name = "MaUuDai", length = 10)
    private String maUuDai;

    @Column(name = "MucUuDai", precision = 5, scale = 2) // Phần trăm ưu đãi
    private BigDecimal mucUuDai;

    @Column(name = "NoiDung", length = 255)
    private String noiDung;

    @OneToMany(mappedBy = "uuDai", fetch = FetchType.LAZY)
    private Set<LoaiKH> loaiKHs;

    @OneToMany(mappedBy = "uuDai", fetch = FetchType.LAZY)
    private Set<HoaDonDV> hoaDonDVs;
}