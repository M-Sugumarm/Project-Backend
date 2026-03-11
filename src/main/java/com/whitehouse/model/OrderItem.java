package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String id;
    private String orderId;     // Reference to Order document
    private String productId;   // Reference to Product document
    private Integer quantity;
    private Double price;
    private String productName; // Denormalized for display
    private String imageUrl;    // Denormalized for display
    private String selectedSize;
    private String selectedColor;
}

