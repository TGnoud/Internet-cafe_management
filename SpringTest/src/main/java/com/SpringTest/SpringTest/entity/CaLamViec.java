package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calamviec")
public class CaLamViec {

    @Id
    @Column(name = "MaCaLamViec", length = 10)
    private String maCaLamViec;

    @Column(name = "HeSoLuong", precision = 5, scale = 2)
    private BigDecimal heSoLuong;

    @Column(name = "ThoiGianVaoLam")
    private LocalTime thoiGianVaoLam;

    @Column(name = "ThoiGianKetThuc")
    private LocalTime thoiGianKetThuc;

    @OneToMany(mappedBy = "caLamViec", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NhanVienCaLamViec> nhanVienCaLamViecs;
}