package com.whitehouse.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private String id;
    private String userId;
    private String userEmail;
    private Double totalAmount;
    private String status;
    private String shippingAddress;
    private String createdAt;
    private String updatedAt;
    private List<OrderItemDTO> items;
}
