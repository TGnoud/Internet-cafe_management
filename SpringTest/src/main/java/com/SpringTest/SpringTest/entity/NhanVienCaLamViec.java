package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nhanvien_calamviec")
public class NhanVienCaLamViec {

    @EmbeddedId
    private NhanVienCaLamViecId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maNV")
    @JoinColumn(name = "MaNV")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maCaLamViec")
    @JoinColumn(name = "MaCaLamViec")
    private CaLamViec caLamViec;

    // Nếu có thêm các cột khác trong bảng NhanVien_CaLamViec (ví dụ: NgayLamViec),
    // bạn sẽ thêm chúng ở đây. DDL của bạn không có, nhưng thực tế có thể cần.
    // @Column(name = "NgayLamViec")
    // private LocalDate ngayLamViec;
}
