package com.whitehouse.controller;

import com.whitehouse.model.Order;
import com.whitehouse.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/admin")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> request) {
        String userId = request.get("userId") != null ? request.get("userId").toString() : "0"; // Default to 0 if null
        String shippingAddress = request.get("shippingAddress").toString();
        String stripePaymentId = request.get("stripePaymentId") != null 
            ? request.get("stripePaymentId").toString() 
            : null;

        try {
            if (request.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
                Order order = orderService.createOrderFromItems(userId, shippingAddress, stripePaymentId, items);
                return ResponseEntity.ok(order);
            } else {
                // Fallback to old behavior (cart-based)
                Order order = orderService.createOrder(userId, shippingAddress, stripePaymentId);
                return ResponseEntity.ok(order);
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(OrderController.class).error("Error creating order", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, request.get("status"));
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public List<Order> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatus(status);
    }
}
