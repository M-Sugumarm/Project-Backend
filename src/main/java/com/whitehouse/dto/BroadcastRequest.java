package com.whitehouse.dto;

import lombok.Data;

@Data
public class BroadcastRequest {
    private String offerType;         // e.g., "DISCOUNT_10", "DISCOUNT_20", "OCCASION"
    private String title;             // e.g., "Diwali Special Offer"
    private String message;           // Custom message from admin
    private String promoCode;         // e.g., "DIWALI20"
    private Integer discountPercent;  // e.g., 10 or 20
}
