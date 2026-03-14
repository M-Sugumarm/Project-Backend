package com.whitehouse.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private Storage storage;

    @Value("${firebase.storage-bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            throw new IOException("Bucket " + bucketName + " not found");
        }

        Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());
        
        // Construct the public URL for Firebase Storage
        // Format: https://firebasestorage.googleapis.com/v0/b/[BUCKET_NAME]/o/[FILE_NAME]?alt=media
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName, 
                URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
    }
}
