package com.SpringTest.SpringTest.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DichVuDTO {
    private String maDV;
    private String tenDV;
    private BigDecimal donGia;
    private String trangThaiDichVu; // "Còn hàng", "Hết hàng"
}