package com.whitehouse.controller;

import com.whitehouse.model.Product;
import com.whitehouse.model.Review;
import com.whitehouse.repository.ProductRepository;
import com.whitehouse.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }
        
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        
        // Update product average rating
        Optional<Product> optionalProduct = productRepository.findById(review.getProductId());
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            List<Review> allReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(review.getProductId());
            
            double sum = 0;
            for (Review r : allReviews) {
                sum += r.getRating();
            }
            double average = allReviews.isEmpty() ? 0 : sum / allReviews.size();
            
            // Format to 1 decimal place safely
            product.setRating(Math.round(average * 10.0) / 10.0);
            product.setReviews(allReviews.size());
            
            productRepository.save(product);
        }
        
        return ResponseEntity.ok(savedReview);
    }
}
