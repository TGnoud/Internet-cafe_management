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
@Table(name = "chucvu")
public class ChucVu {

    @Id
    @Column(name = "MaChucVu", length = 10)
    private String maChucVu;

    @Column(name = "TenChucVu", nullable = false, length = 45)
    private String tenChucVu;

    @Column(name = "LuongTheoGio", precision = 10, scale = 2)
    private BigDecimal luongTheoGio;

    @OneToMany(mappedBy = "chucVu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NhanVien> nhanViens;
}