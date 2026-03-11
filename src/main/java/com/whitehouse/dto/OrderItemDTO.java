package com.whitehouse.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String id;
    private String productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Double price;
    private String selectedSize;
    private String selectedColor;
}
