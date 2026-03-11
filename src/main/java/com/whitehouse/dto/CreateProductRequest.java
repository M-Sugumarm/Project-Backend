package com.whitehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private Double price;

    private Double originalPrice;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;

    private String imageUrl;

    @NotNull(message = "Category ID is required")
    private String categoryId;

    private Boolean isFeatured = false;
    private Boolean isUpcoming = false;
    private String sizes;
    private String colors;
    private String material;
    private String brand = "Jay Shree";
}
