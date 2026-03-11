package com.whitehouse.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
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

    @Value("${firebase.service-account}")
    private Resource serviceAccount;

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials.json:#{null}}")
    private String firebaseCredentialsJson;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream;
            
            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                // Use environment variable string (for cloud deployment)
                credentialsStream = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
                System.out.println("Initializing Firebase using FIREBASE_CREDENTIALS environment variable...");
            } else {
                // Fallback to local file
                credentialsStream = serviceAccount.getInputStream();
                System.out.println("Initializing Firebase using local service-account file fallback...");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully for project: " + projectId);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
