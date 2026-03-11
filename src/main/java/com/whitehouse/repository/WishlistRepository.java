package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.WishlistItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class WishlistRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "wishlist_items";

    public List<WishlistItem> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<WishlistItem> wishlistItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                WishlistItem wishlistItem = document.toObject(WishlistItem.class);
                wishlistItem.setId(document.getId());
                wishlistItems.add(wishlistItem);
            }
            return wishlistItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching wishlist items", e);
        }
    }

    public Optional<WishlistItem> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                WishlistItem wishlistItem = document.toObject(WishlistItem.class);
                wishlistItem.setId(document.getId());
                return Optional.of(wishlistItem);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching wishlist item by id", e);
        }
    }

    public WishlistItem save(WishlistItem wishlistItem) {
        try {
            if (wishlistItem.getId() == null || wishlistItem.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                wishlistItem.setId(docRef.getId());
                docRef.set(wishlistItem).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(wishlistItem.getId()).set(wishlistItem).get();
            }
            return wishlistItem;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving wishlist item", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting wishlist item", e);
        }
    }

    public List<WishlistItem> findByUserId(String userId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<WishlistItem> wishlistItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                WishlistItem wishlistItem = document.toObject(WishlistItem.class);
                wishlistItem.setId(document.getId());
                wishlistItems.add(wishlistItem);
            }
            return wishlistItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching wishlist items by user", e);
        }
    }

    public void deleteByUserIdAndProductId(String userId, String productId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", productId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting wishlist item", e);
        }
    }
}
