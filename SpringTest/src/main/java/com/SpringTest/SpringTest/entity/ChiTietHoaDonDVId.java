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
public class ChiTietHoaDonDVId implements Serializable {

    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    @Column(name = "MaHD", length = 15)
    private String maHD;

    @Column(name = "MaDV", length = 10)
    private String maDV;

    // equals and hashCode are crucial for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDonDVId that = (ChiTietHoaDonDVId) o;
        return Objects.equals(maHD, that.maHD) &&
                Objects.equals(maDV, that.maDV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHD, maDV);
    }
}