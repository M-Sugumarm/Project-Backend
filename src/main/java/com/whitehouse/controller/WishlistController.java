package com.whitehouse.controller;

import com.whitehouse.model.WishlistItem;
import com.whitehouse.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/{userId}")
    public List<WishlistItem> getWishlist(@PathVariable String userId) {
        return wishlistService.getWishlist(userId);
    }

    @PostMapping("/add")
    public ResponseEntity<WishlistItem> addToWishlist(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String productId = request.get("productId");
        WishlistItem item = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable String userId,
            @PathVariable String productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{userId}/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            @PathVariable String userId,
            @PathVariable String productId) {
        boolean inWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Integer>> getWishlistCount(@PathVariable String userId) {
        int count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
