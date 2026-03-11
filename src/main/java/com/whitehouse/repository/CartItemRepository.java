package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class CartItemRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "cart_items";

    public List<CartItem> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<CartItem> cartItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                CartItem cartItem = document.toObject(CartItem.class);
                cartItem.setId(document.getId());
                cartItems.add(cartItem);
            }
            return cartItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching cart items", e);
        }
    }

    public Optional<CartItem> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                CartItem cartItem = document.toObject(CartItem.class);
                cartItem.setId(document.getId());
                return Optional.of(cartItem);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching cart item by id", e);
        }
    }

    public CartItem save(CartItem cartItem) {
        try {
            if (cartItem.getId() == null || cartItem.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                cartItem.setId(docRef.getId());
                docRef.set(cartItem).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(cartItem.getId()).set(cartItem).get();
            }
            return cartItem;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving cart item", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting cart item", e);
        }
    }

    public List<CartItem> findByUserId(String userId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<CartItem> cartItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                CartItem cartItem = document.toObject(CartItem.class);
                cartItem.setId(document.getId());
                cartItems.add(cartItem);
            }
            return cartItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching cart items by user", e);
        }
    }

    public void deleteByUserId(String userId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting cart items by user", e);
        }
    }
}
