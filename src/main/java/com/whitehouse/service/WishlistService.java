package com.whitehouse.service;

import com.whitehouse.model.Product;
import com.whitehouse.model.WishlistItem;
import com.whitehouse.repository.ProductRepository;
import com.whitehouse.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<WishlistItem> getWishlist(String userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public WishlistItem addToWishlist(String userId, String productId) {
        // Check if product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already in wishlist
        List<WishlistItem> wishlist = wishlistRepository.findByUserId(userId);
        for (WishlistItem item : wishlist) {
            if (item.getProductId().equals(productId)) {
                return item; // Already in wishlist
            }
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setAddedAt(LocalDateTime.now());

        return wishlistRepository.save(item);
    }

    public void removeFromWishlist(String userId, String productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public boolean isInWishlist(String userId, String productId) {
        List<WishlistItem> wishlist = wishlistRepository.findByUserId(userId);
        return wishlist.stream().anyMatch(item -> item.getProductId().equals(productId));
    }

    public int getWishlistCount(String userId) {
        return wishlistRepository.findByUserId(userId).size();
    }
}
