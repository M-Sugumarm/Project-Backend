package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class OrderRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "orders";

    public List<Order> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Order> orders = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                orders.add(mapToOrder(document));
            }
            return orders;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching orders", e);
        }
    }

    public Optional<Order> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return Optional.of(mapToOrder(document));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching order by id", e);
        }
    }

    public Order save(Order order) {
        try {
            Map<String, Object> orderMap = new java.util.HashMap<>();
            orderMap.put("id", order.getId()); // Use current ID if exists
            orderMap.put("userId", order.getUserId());
            orderMap.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
            orderMap.put("status", order.getStatus());
            orderMap.put("stripePaymentId", order.getStripePaymentId());
            orderMap.put("shippingAddress", order.getShippingAddress());
            orderMap.put("createdAt", order.getCreatedAt());
            orderMap.put("updatedAt", order.getUpdatedAt());
            
            List<Map<String, Object>> itemsList = new java.util.ArrayList<>();
            if (order.getItems() != null) {
                for (com.whitehouse.model.OrderItem item : order.getItems()) {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("orderId", order.getId());
                    itemMap.put("productId", item.getProductId());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice() != null ? item.getPrice() : 0.0);
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("imageUrl", item.getImageUrl());
                    itemMap.put("selectedSize", item.getSelectedSize());
                    itemMap.put("selectedColor", item.getSelectedColor());
                    itemsList.add(itemMap);
                }
            }
            orderMap.put("items", itemsList);

            if (order.getId() == null || order.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                order.setId(docRef.getId());
                orderMap.put("id", order.getId());
                docRef.set(orderMap).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(order.getId()).set(orderMap).get();
            }
            return order;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving order", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting order", e);
        }
    }

    public List<Order> findByUserId(String userId) {
        System.out.println("Entering findByUserId for: " + userId);
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Order> orders = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                orders.add(mapToOrder(document));
            }
            // Sort in memory to avoid needing a composite index
            orders.sort((o1, o2) -> {
                String t1 = o1.getCreatedAt();
                String t2 = o2.getCreatedAt();
                if (t1 == null) return 1;
                if (t2 == null) return -1;
                return t2.compareTo(t1); // Descending
            });
            return orders;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("ERROR in findByUserId: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching orders by user", e);
        }
    }

    public List<Order> findByStatus(String status) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", status)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Order> orders = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                orders.add(mapToOrder(document));
            }
            return orders;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching orders by status", e);
        }
    }

    private Order mapToOrder(DocumentSnapshot document) {
        Order order = new Order();
        order.setId(document.getId());
        order.setUserId(document.getString("userId"));
        order.setTotalAmount(asDouble(document.get("totalAmount")));
        order.setStatus(document.getString("status"));
        order.setStripePaymentId(document.getString("stripePaymentId"));
        order.setShippingAddress(document.getString("shippingAddress"));
        order.setCreatedAt(document.getString("createdAt"));
        order.setUpdatedAt(document.getString("updatedAt"));

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) document.get("items");
        if (itemsData != null) {
            List<com.whitehouse.model.OrderItem> items = new ArrayList<>();
            for (Map<String, Object> itemMap : itemsData) {
                com.whitehouse.model.OrderItem item = new com.whitehouse.model.OrderItem();
                item.setId((String) itemMap.get("id"));
                item.setProductId((String) itemMap.get("productId"));
                item.setQuantity(itemMap.get("quantity") != null ? ((Number) itemMap.get("quantity")).intValue() : 0);
                item.setPrice(asDouble(itemMap.get("price")));
                item.setProductName((String) itemMap.get("productName"));
                item.setImageUrl((String) itemMap.get("imageUrl"));
                item.setSelectedSize((String) itemMap.get("selectedSize"));
                item.setSelectedColor((String) itemMap.get("selectedColor"));
                items.add(item);
            }
            order.setItems(items);
        }
        return order;
    }

    private Double asDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
