package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "maytinh")
public class MayTinh {

    @Id
    @Column(name = "MaMay", length = 10)
    private String maMay;

    @Column(name = "TenMay", nullable = false, length = 45)
    private String tenMay;

    @Column(name = "TrangThai", length = 45) // Ví dụ: Khả dụng, Bảo trì
    private String trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLoaiMay", nullable = false)
    private LoaiMay loaiMay;

    @OneToMany(mappedBy = "mayTinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PhienSuDung> phienSuDungs;
}