package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String id;
    private String userId;      // Reference to User document
    private String productId;   // Reference to Product document
    private Integer quantity;
    private String selectedSize;
    private String selectedColor;
}

