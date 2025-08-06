package com.SpringTest.SpringTest.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PhienSuDungInfoResponse {
    private Integer maPhien;
    private String maMay;
    private String tenMay;
    private String maTK;
    private String tenTK;
    private LocalDateTime thoiGianBatDau;
    private BigDecimal soTienConLai;
    private Long thoiGianConLaiDuKienPhut; // Thời gian còn lại dự kiến bằng phút
    private LocalDateTime thoiGianKetThuc; // Thêm trường này
    private BigDecimal tongTienPhien; // Có thể thêm nếu muốn hiển thị tiền của phiên đó


}