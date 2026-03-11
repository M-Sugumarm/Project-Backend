package com.whitehouse.controller;

import com.whitehouse.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Subscribe to offer/discount emails.
     * Body: { "email": "...", "name": "..." }
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name  = body.get("name");
        Map<String, Object> result = subscriptionService.subscribe(email, name);
        return ResponseEntity.ok(result);
    }

    /**
     * Unsubscribe from offer/discount emails.
     * Body: { "email": "..." }
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, Object> result = subscriptionService.unsubscribe(email);
        return ResponseEntity.ok(result);
    }

    /**
     * Admin endpoint: Send a broadcast offer to all subscribed users
     */
    @PostMapping("/subscribe/broadcast")
    public ResponseEntity<Map<String, Object>> sendBroadcastOffer(@RequestBody com.whitehouse.dto.BroadcastRequest request) {
        try {
            subscriptionService.sendBroadcast(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Broadcast offers are being sent asynchronously."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to start broadcast: " + e.getMessage()
            ));
        }
    }
}
