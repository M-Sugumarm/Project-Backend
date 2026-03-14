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

    @Value("${firebase.storage-bucket}")
    private String storageBucket;

    @Value("${FIREBASE_CREDENTIALS:#{null}}")
    private String firebaseCredentialsJson;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream;
            
            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                credentialsStream = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
            } else if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
                credentialsStream = new java.io.FileInputStream(serviceAccountPath.replace("classpath:", "src/main/resources/"));
            } else {
                throw new IOException("No Firebase credentials provided");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public Storage storage() throws IOException {
        InputStream credentialsStream;
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
            credentialsStream = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
        } else if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
            credentialsStream = new java.io.FileInputStream(serviceAccountPath.replace("classpath:", "src/main/resources/"));
        } else {
            return com.google.cloud.storage.StorageOptions.getDefaultInstance().getService();
        }

        return com.google.cloud.storage.StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .setProjectId(projectId)
                .build()
                .getService();
    }
}
