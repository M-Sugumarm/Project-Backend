package com.whitehouse.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private String imageUrl;
    private String categoryId;
    private String categoryName;
    private Boolean isFeatured;
    private Boolean isUpcoming;
    private Double rating;
    private Integer reviews;
    private String sizes;
    private String colors;
    private String material;
    private String brand;
}
