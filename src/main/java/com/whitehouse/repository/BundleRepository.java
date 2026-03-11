package com.whitehouse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.whitehouse.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class BundleRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "bundles";

    public List<Bundle> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Bundle> bundles = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Bundle bundle = document.toObject(Bundle.class);
                bundle.setId(document.getId());
                bundles.add(bundle);
            }
            return bundles;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching bundles", e);
        }
    }

    public Optional<Bundle> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Bundle bundle = document.toObject(Bundle.class);
                bundle.setId(document.getId());
                return Optional.of(bundle);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching bundle by id", e);
        }
    }

    public Bundle save(Bundle bundle) {
        try {
            if (bundle.getId() == null || bundle.getId().isEmpty()) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                bundle.setId(docRef.getId());
                docRef.set(bundle).get();
            } else {
                firestore.collection(COLLECTION_NAME).document(bundle.getId()).set(bundle).get();
            }
            return bundle;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving bundle", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting bundle", e);
        }
    }
}
