package com.whitehouse.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account:#{null}}")
    private String serviceAccountPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${FIREBASE_CREDENTIALS:#{null}}")
    private String firebaseCredentialsJson;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream;
            
            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                // Use environment variable string (for cloud deployment)
                credentialsStream = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
                System.out.println("Initializing Firebase using FIREBASE_CREDENTIALS environment variable...");
            } else if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
                // Fallback to local file if provided
                try {
                    credentialsStream = new java.io.FileInputStream(serviceAccountPath.replace("classpath:", "src/main/resources/"));
                    System.out.println("Initializing Firebase using local service-account file: " + serviceAccountPath);
                } catch (IOException e) {
                    System.err.println("Failed to load local Firebase credentials from: " + serviceAccountPath);
                    throw e;
                }
            } else {
                throw new IOException("No Firebase credentials provided (FIREBASE_CREDENTIALS env var or firebase.service-account property)");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .setProjectId(projectId)
                    .setStorageBucket(projectId + ".appspot.com")
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully for project: " + projectId);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public Storage storage() {
        return StorageClient.getInstance().bucket().getStorage();
    }
}
