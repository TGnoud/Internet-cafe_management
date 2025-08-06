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
@Table(name = "loaimay")
public class LoaiMay {

    @Id
    @Column(name = "MaLoaiMay", length = 10)
    private String maLoaiMay;

    @Column(name = "GiaTheoGio", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTheoGio;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @OneToMany(mappedBy = "loaiMay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MayTinh> mayTinhs;
}