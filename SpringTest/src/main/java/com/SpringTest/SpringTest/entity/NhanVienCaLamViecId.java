package com.SpringTest.SpringTest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class NhanVienCaLamViecId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "MaNV", length = 10)
    private String maNV;

    @Column(name = "MaCaLamViec", length = 10)
    private String maCaLamViec;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhanVienCaLamViecId that = (NhanVienCaLamViecId) o;
        return Objects.equals(maNV, that.maNV) &&
                Objects.equals(maCaLamViec, that.maCaLamViec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNV, maCaLamViec);
    }
}