package com.whitehouse.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private String id;
    private String productId;
    private String userId;
    private String userName; 
    private int rating;      
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
