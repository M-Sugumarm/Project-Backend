package com.whitehouse.service;

import com.whitehouse.model.User;
import com.whitehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Subscribe a user (by email) to offer/discount emails.
     * If the user already exists in Firestore, just flip the flag.
     * If they are a guest (no account), create a lightweight record.
     */
    public Map<String, Object> subscribe(String email, String name) {
        Map<String, Object> result = new HashMap<>();

        if (email == null || email.isBlank()) {
            result.put("success", false);
            result.put("message", "Email address is required.");
            return result;
        }

        String displayName = (name != null && !name.isBlank()) ? name : "Valued Customer";

        Optional<User> existing = userRepository.findByEmail(email.trim().toLowerCase());

        if (existing.isPresent()) {
            User user = existing.get();
            if (Boolean.TRUE.equals(user.getEmailSubscribed())) {
                result.put("success", true);
                result.put("message", "You are already subscribed! 🎉");
                return result;
            }
            user.setEmailSubscribed(true);
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(displayName);
            }
            userRepository.save(user);
        } else {
            // Guest user — create a subscription-only record
            User newUser = new User();
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setName(displayName);
            newUser.setEmailSubscribed(true);
            newUser.setIsAdmin(false);
            userRepository.save(newUser);
        }

        // Send welcome email asynchronously
        try {
            emailService.sendWelcomeSubscriptionEmail(email.trim().toLowerCase(), displayName);
        } catch (Exception e) {
            System.err.println("⚠️ Welcome email failed for: " + email + " — " + e.getMessage());
        }

        result.put("success", true);
        result.put("message", "Successfully subscribed! Check your inbox for a welcome gift. 🎁");
        return result;
    }

    /**
     * Unsubscribe a user by email.
     */
    public Map<String, Object> unsubscribe(String email) {
        Map<String, Object> result = new HashMap<>();

        if (email == null || email.isBlank()) {
            result.put("success", false);
            result.put("message", "Email address is required.");
            return result;
        }

        Optional<User> existing = userRepository.findByEmail(email.trim().toLowerCase());
        if (existing.isPresent()) {
            User user = existing.get();
            user.setEmailSubscribed(false);
            userRepository.save(user);
            result.put("success", true);
            result.put("message", "You have been unsubscribed. We'll miss you!");
        } else {
            result.put("success", false);
            result.put("message", "Email not found in our subscriber list.");
        }
        return result;
    }

    /**
     * Send a broadcast offer (discount or occasion) to all subscribed users.
     */
    public void sendBroadcast(com.whitehouse.dto.BroadcastRequest request) {
        // Validate request
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Broadcast title is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Broadcast message is required");
        }
        if (request.getOfferType() == null || request.getOfferType().trim().isEmpty()) {
            throw new IllegalArgumentException("Offer type is required");
        }
        
        // Delegate to email service to send asynchronously to subscribed users
        emailService.sendBroadcastOffers(request);
    }
}
