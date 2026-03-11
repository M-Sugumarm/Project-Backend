package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class ProductRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "products";

    public List<Product> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                products.add(mapToProduct(document));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching products", e);
        }
    }

    public Optional<Product> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return Optional.of(mapToProduct(document));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching product by id", e);
        }
    }

    public Product save(Product product) {
        try {
            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("name", product.getName());
            productMap.put("description", product.getDescription());
            productMap.put("price", product.getPrice());
            productMap.put("originalPrice", product.getOriginalPrice());
            productMap.put("stock", product.getStock());
            productMap.put("imageUrl", product.getImageUrl());
            productMap.put("categoryId", product.getCategoryId());
            productMap.put("isFeatured", product.getIsFeatured());
            productMap.put("isUpcoming", product.getIsUpcoming());
            productMap.put("rating", product.getRating());
            productMap.put("reviews", product.getReviews());
            productMap.put("sizes", product.getSizes());
            productMap.put("colors", product.getColors());
            productMap.put("material", product.getMaterial());
            productMap.put("brand", product.getBrand());

            if (product.getId() == null || product.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                product.setId(docRef.getId());
                docRef.set(productMap).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(product.getId()).set(productMap).get();
            }
            return product;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    public List<Product> findByCategoryId(String categoryId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("categoryId", categoryId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                products.add(mapToProduct(document));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching products by category", e);
        }
    }

    public List<Product> findByIsFeaturedTrue() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("isFeatured", true)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                products.add(mapToProduct(document));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching featured products", e);
        }
    }

    public List<Product> findByIsUpcomingTrue() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("isUpcoming", true)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                products.add(mapToProduct(document));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching upcoming products", e);
        }
    }

    public List<Product> search(String query) {
        try {
            // Firestore doesn't support full-text search natively
            // We'll search by name containing the query (case-insensitive)
            List<Product> allProducts = findAll();
            List<Product> results = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Product product : allProducts) {
                if (product.getName() != null && product.getName().toLowerCase().contains(lowerQuery) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery))) {
                    results.add(product);
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error searching products", e);
        }
    }

    public List<Product> findByStockLessThan(Integer threshold) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereLessThan("stock", threshold)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Product> products = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                products.add(mapToProduct(document));
            }
            return products;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching low stock products", e);
        }
    }

    private Product mapToProduct(DocumentSnapshot document) {
        Product product = new Product();
        product.setId(document.getId());
        product.setName(document.getString("name"));
        product.setDescription(document.getString("description"));
        product.setPrice(asDouble(document.get("price")));
        product.setOriginalPrice(asDouble(document.get("originalPrice")));
        product.setStock(document.getLong("stock") != null ? document.getLong("stock").intValue() : 0);
        product.setImageUrl(document.getString("imageUrl"));
        product.setCategoryId(document.getString("categoryId"));
        product.setIsFeatured(document.getBoolean("isFeatured"));
        product.setIsUpcoming(document.getBoolean("isUpcoming"));
        product.setRating(asDouble(document.get("rating")));
        product.setReviews(document.getLong("reviews") != null ? document.getLong("reviews").intValue() : 0);
        product.setSizes(document.getString("sizes"));
        product.setColors(document.getString("colors"));
        product.setMaterial(document.getString("material"));
        product.setBrand(document.getString("brand"));
        return product;
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
