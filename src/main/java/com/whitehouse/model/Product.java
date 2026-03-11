package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private String imageUrl;
    private String categoryId;  // Reference to Category document
    private Boolean isFeatured = false;
    private Boolean isUpcoming = false;
    private Double rating = 0.0;
    private Integer reviews = 0;
    private String sizes;
    private String colors;
    private String material;
    private String brand = "Jay Shree";
}

