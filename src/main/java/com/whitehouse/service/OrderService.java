package com.whitehouse.service;

import com.whitehouse.model.*;
import com.whitehouse.repository.CartItemRepository;
import com.whitehouse.repository.OrderRepository;
import com.whitehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SseService sseService;

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order createOrder(String userId, String shippingAddress, String stripePaymentId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setStripePaymentId(stripePaymentId);
        order.setStatus("PROCESSING");
        order.setCreatedAt(LocalDateTime.now().toString());
        order.setUpdatedAt(LocalDateTime.now().toString());

        List<OrderItem> orderItems = new ArrayList<>();
        Double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setImageUrl(product.getImageUrl());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice() != null ? product.getPrice() : 0.0);
            orderItem.setSelectedSize(cartItem.getSelectedSize());
            orderItem.setSelectedColor(cartItem.getSelectedColor());
            orderItems.add(orderItem);

            Double itemTotal = (product.getPrice() != null ? product.getPrice() : 0.0) * cartItem.getQuantity();
            totalAmount += itemTotal;
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear the cart after order is placed
        cartItemRepository.deleteByUserId(userId);

        sseService.broadcast("NEW_ORDER", savedOrder);

        return savedOrder;
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now().toString());
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    public Order createOrderFromItems(String userId, String shippingAddress, String stripePaymentId, List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Order items are empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setStripePaymentId(stripePaymentId);
        order.setStatus("PROCESSING");
        order.setCreatedAt(LocalDateTime.now().toString());
        order.setUpdatedAt(LocalDateTime.now().toString());

        List<OrderItem> orderItems = new ArrayList<>();
        Double totalAmount = 0.0;

        for (Map<String, Object> itemData : items) {
            String productId = itemData.get("id").toString(); // Frontend sends 'id'
            Integer quantity = Integer.parseInt(itemData.get("quantity").toString());
            String size = itemData.get("size") != null ? itemData.get("size").toString() : null;
            String color = itemData.get("color") != null ? itemData.get("color").toString() : null;

            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setImageUrl(product.getImageUrl());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice() != null ? product.getPrice() : 0.0);
            orderItem.setSelectedSize(size);
            orderItem.setSelectedColor(color);
            
            // Set back-reference if needed, typically JPA handles this if mapped correctly, 
            // but for simplicity assuming Unidirectional or handled by cascade
            // orderItem.setOrder(order); 
            
            orderItems.add(orderItem);

            Double itemTotal = (product.getPrice() != null ? product.getPrice() : 0.0) * quantity;
            totalAmount += itemTotal;
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        sseService.broadcast("NEW_ORDER", savedOrder);
        
        return savedOrder;
    }
}
