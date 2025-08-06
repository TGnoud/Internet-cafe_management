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
@Table(name = "loaikh")
public class LoaiKH {

    @Id
    @Column(name = "MaLoaiKH", length = 10)
    private String maLoaiKH;

    @Column(name = "TenLoai", nullable = false, length = 45)
    private String tenLoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaUuDai") // MaUuDai có thể null
    private UuDai uuDai;

    @OneToMany(mappedBy = "loaiKH", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<KhachHang> khachHangs;
}