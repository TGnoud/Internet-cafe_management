package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDonDV")
public class HoaDonDV {

    @Id
    @Column(name = "MaHD", length = 15)
    private String maHD;

    @ManyToOne
    @JoinColumn(name = "maTK")
    private TaiKhoan taiKhoan;

    @OneToMany(mappedBy = "hoaDonDV", cascade = CascadeType.ALL)
    private List<ChiTietHoaDonDV> chiTiet;

    @ManyToOne
    @JoinColumn(name = "maUuDai")
    private UuDai uuDai;


    @Column(name = "ThoiDiemThanhToan", nullable = false)
    private LocalDateTime thoiDiemThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV") // MaNV có thể null
    private NhanVien nhanVien;

    @OneToMany(mappedBy = "hoaDonDV", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChiTietHoaDonDV> chiTietHoaDonDVs;

    public void setNgayLap(LocalDateTime now) {
    }

    // Getter và Setter cho maTK
    public String getMaTK() {
        return taiKhoan != null ? taiKhoan.getMaTK() : null;
    }

    public void setMaTK(String maTK) {
        if (taiKhoan == null) {
            taiKhoan = new TaiKhoan();
        }
        taiKhoan.setMaTK(maTK);
    }

    // Getter và Setter cho maDV
    public String getMaDV() {
        return chiTiet != null && !chiTiet.isEmpty() ? chiTiet.get(0).getId().getMaDV() : null;
    }

    public void setMaDV(String maDV) {
        if (chiTiet == null || chiTiet.isEmpty()) {
            ChiTietHoaDonDV chiTietMoi = new ChiTietHoaDonDV();
            chiTietMoi.setHoaDonDV(this);
            ChiTietHoaDonDVId id = new ChiTietHoaDonDVId();
            id.setMaHD(this.maHD);
            id.setMaDV(maDV);
            chiTietMoi.setId(id);
            chiTiet = List.of(chiTietMoi);
        } else {
            ChiTietHoaDonDVId id = chiTiet.get(0).getId();
            id.setMaDV(maDV);
            chiTiet.get(0).setId(id);
        }
    }

    // Getter và Setter cho maUuDai
    public String getMaUuDai() {
        return uuDai != null ? uuDai.getMaUuDai() : null;
    }

    public void setMaUuDai(String maUuDai) {
        if (uuDai == null) {
            uuDai = new UuDai();
        }
        uuDai.setMaUuDai(maUuDai);
    }
}