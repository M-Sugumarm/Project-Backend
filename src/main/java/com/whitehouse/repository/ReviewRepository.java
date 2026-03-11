package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class ReviewRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "reviews";

    public List<Review> findByProductIdOrderByCreatedAtDesc(String productId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("productId", productId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Review> reviews = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                reviews.add(mapToReview(document));
            }
            return reviews;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching reviews for product", e);
        }
    }

    public Review save(Review review) {
        try {
            if (review.getCreatedAt() == null) {
                review.setCreatedAt(LocalDateTime.now());
            }

            Map<String, Object> reviewMap = new java.util.HashMap<>();
            reviewMap.put("productId", review.getProductId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("userName", review.getUserName());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("comment", review.getComment());
            reviewMap.put("createdAt", review.getCreatedAt().toString()); // Store as ISO string

            if (review.getId() == null || review.getId().isEmpty()) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                review.setId(docRef.getId());
                docRef.set(reviewMap).get();
            } else {
                firestore.collection(COLLECTION_NAME).document(review.getId()).set(reviewMap).get();
            }
            return review;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving review", e);
        }
    }

    private Review mapToReview(DocumentSnapshot document) {
        Review review = new Review();
        review.setId(document.getId());
        review.setProductId(document.getString("productId"));
        review.setUserId(document.getString("userId"));
        review.setUserName(document.getString("userName"));
        review.setRating(document.getLong("rating") != null ? document.getLong("rating").intValue() : 0);
        review.setComment(document.getString("comment"));
        
        String createdAtStr = document.getString("createdAt");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            review.setCreatedAt(LocalDateTime.parse(createdAtStr));
        }
        
        return review;
    }
}
