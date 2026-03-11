package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class CategoryRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "categories";

    public List<Category> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Category> categories = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Category category = document.toObject(Category.class);
                category.setId(document.getId());
                categories.add(category);
            }
            return categories;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching categories", e);
        }
    }

    public Optional<Category> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Category category = document.toObject(Category.class);
                category.setId(document.getId());
                return Optional.of(category);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching category by id", e);
        }
    }

    public Category save(Category category) {
        try {
            if (category.getId() == null || category.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                category.setId(docRef.getId());
                docRef.set(category).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(category.getId()).set(category).get();
            }
            return category;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving category", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    public long count() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            return future.get().size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error counting categories", e);
        }
    }

    public Optional<Category> findByName(String name) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("name", name)
                    .limit(1)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                Category category = documents.get(0).toObject(Category.class);
                category.setId(documents.get(0).getId());
                return Optional.of(category);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching category by name", e);
        }
    }
}
