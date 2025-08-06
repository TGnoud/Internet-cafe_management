package com.SpringTest.SpringTest.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class NapTienRequest {
    private String maTK;
    private BigDecimal soTienNap;

    public NapTienRequest(String maTK, Object o) {
    }

    public BigDecimal getSoTien() {
        return soTienNap;
    }
}