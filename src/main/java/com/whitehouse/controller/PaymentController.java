package com.whitehouse.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Long amount = Long.valueOf(request.get("amount").toString());
            String currency = request.get("currency") != null 
                ? request.get("currency").toString() 
                : "inr";
            
            // Get description from request or use default
            String description = request.get("description") != null
                ? request.get("description").toString()
                : "Purchase from Jay Shree Textiles E-Commerce";
            
            // Get shipping information (required for Indian export regulations)
            @SuppressWarnings("unchecked")
            Map<String, String> shipping = (Map<String, String>) request.get("shipping");

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amount * 100) // Convert to paise
                .setCurrency(currency)
                .setDescription(description) // Required for Indian export regulations
                .setStatementDescriptorSuffix("JayShree") // Appears on customer's statement
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                );
            
            // Add shipping information if provided (required for Indian regulations)
            if (shipping != null && shipping.containsKey("name") && shipping.containsKey("address")) {
                paramsBuilder.setShipping(
                    PaymentIntentCreateParams.Shipping.builder()
                        .setName(shipping.get("name"))
                        .setAddress(
                            PaymentIntentCreateParams.Shipping.Address.builder()
                                .setLine1(shipping.get("address"))
                                .setCity(shipping.get("city"))
                                .setState(shipping.get("state"))
                                .setPostalCode(shipping.get("postalCode"))
                                .setCountry("IN")
                                .build()
                        )
                        .build()
                );
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody Map<String, String> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId");
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("id", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

