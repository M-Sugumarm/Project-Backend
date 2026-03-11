package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class OrderItemRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "order_items";

    public List<OrderItem> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<OrderItem> orderItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                OrderItem orderItem = document.toObject(OrderItem.class);
                orderItem.setId(document.getId());
                orderItems.add(orderItem);
            }
            return orderItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching order items", e);
        }
    }

    public Optional<OrderItem> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                OrderItem orderItem = document.toObject(OrderItem.class);
                orderItem.setId(document.getId());
                return Optional.of(orderItem);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching order item by id", e);
        }
    }

    public OrderItem save(OrderItem orderItem) {
        try {
            if (orderItem.getId() == null || orderItem.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                orderItem.setId(docRef.getId());
                docRef.set(orderItem).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(orderItem.getId()).set(orderItem).get();
            }
            return orderItem;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving order item", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting order item", e);
        }
    }

    public List<OrderItem> findByOrderId(String orderId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("orderId", orderId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<OrderItem> orderItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                OrderItem orderItem = document.toObject(OrderItem.class);
                orderItem.setId(document.getId());
                orderItems.add(orderItem);
            }
            return orderItems;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching order items by order", e);
        }
    }
}
