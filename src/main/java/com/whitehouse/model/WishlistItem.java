package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {
    private String id;
    private String userId;      // Reference to User document
    private String productId;   // Reference to Product document
    private LocalDateTime addedAt = LocalDateTime.now();
}
