package com.SpringTest.SpringTest.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateHoaDonRequest {
    private String maTK; // Mã tài khoản khách hàng (nếu có)
    private String maNV; // Mã nhân viên lập hóa đơn (lấy từ Principal hoặc nhân viên tự nhập)
    private String maUuDai; // Optional
    private List<OrderServiceRequest.OrderItemRequest> items; // Danh sách các dịch vụ trong OrderServiceRequest đã có


    // OrderItemRequest đã được định nghĩa trong OrderServiceRequest
    // @Data
    // public static class OrderItemRequest {
    //     private String maDV;
    //     private int soLuong;
    // }
}