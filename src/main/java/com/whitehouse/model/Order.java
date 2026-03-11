package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String userId;  // Reference to User document
    private Double totalAmount;
    private String status = "PENDING";
    private String stripePaymentId;
    private String shippingAddress;
    private String createdAt;
    private String updatedAt;
    private List<OrderItem> items;  // Embedded items
}

