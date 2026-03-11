package com.whitehouse.service;

import com.whitehouse.model.Product;
import com.whitehouse.model.User;
import com.whitehouse.model.WishlistItem;
import com.whitehouse.repository.ProductRepository;
import com.whitehouse.repository.UserRepository;
import com.whitehouse.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Run every day at 10 AM (Cron: "0 0 10 * * ?")
    // For immediate demonstration, could be set to run more often, but daily is standard.
    @Scheduled(cron = "0 0 10 * * ?")
    public void scanAndAlertWishlists() {
        System.out.println("⏰ Running daily Wishlist Notification scan...");

        List<WishlistItem> allWishlistItems = wishlistRepository.findAll();

        for (WishlistItem item : allWishlistItems) {
            Optional<Product> optionalProduct = productRepository.findById(item.getProductId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                
                String triggerReason = null;
                
                // Low stock condition
                if (product.getStock() != null && product.getStock() > 0 && product.getStock() <= 5) {
                    triggerReason = "Only " + product.getStock() + " left in stock!";
                } 
                // Price drop condition
                else if (product.getOriginalPrice() != null && product.getPrice() != null && product.getPrice() < product.getOriginalPrice()) {
                    triggerReason = "Price Dropped!";
                }
                
                if (triggerReason != null) {
                    // Send alert implicitly
                    Optional<User> optionalUser = userRepository.findByClerkId(item.getUserId());
                    if (optionalUser.isPresent()) {
                        emailService.sendWishlistNotification(optionalUser.get(), product, triggerReason);
                    }
                }
            }
        }
    }
}
