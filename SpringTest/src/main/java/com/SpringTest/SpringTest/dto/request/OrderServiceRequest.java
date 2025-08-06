package com.SpringTest.SpringTest.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderServiceRequest {
    private String maTK; // Hoặc MaPhien
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private String maDV;
        private int soLuong;
    }
}