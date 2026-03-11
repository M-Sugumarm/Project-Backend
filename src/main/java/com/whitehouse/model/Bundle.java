package com.whitehouse.model;

import lombok.Data;
import java.util.List;

@Data
public class Bundle {
    private String id;
    private String name;
    private String description;
    private List<String> productIds;
    private Double bundlePrice;
    private String imageUrl;
}
