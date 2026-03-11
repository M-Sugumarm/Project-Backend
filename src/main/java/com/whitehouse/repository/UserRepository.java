package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "users";

    public List<User> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                User user = document.toObject(User.class);
                user.setId(document.getId());
                users.add(user);
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching users", e);
        }
    }

    public Optional<User> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                User user = document.toObject(User.class);
                user.setId(document.getId());
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching user by id", e);
        }
    }

    public User save(User user) {
        try {
            if (user.getId() == null || user.getId().isEmpty()) {
                // Create new document with auto-generated ID
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                user.setId(docRef.getId());
                docRef.set(user).get();
            } else {
                // Update existing document
                firestore.collection(COLLECTION_NAME).document(user.getId()).set(user).get();
            }
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                User user = documents.get(0).toObject(User.class);
                user.setId(documents.get(0).getId());
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching user by email", e);
        }
    }

    public Optional<User> findByClerkId(String clerkId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("clerkId", clerkId)
                    .limit(1)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                User user = documents.get(0).toObject(User.class);
                user.setId(documents.get(0).getId());
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching user by clerk id", e);
        }
    }

    public List<User> findAllByEmailSubscribed(Boolean subscribed) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("emailSubscribed", subscribed)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                User user = document.toObject(User.class);
                user.setId(document.getId());
                users.add(user);
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching subscribed users", e);
        }
    }
}
