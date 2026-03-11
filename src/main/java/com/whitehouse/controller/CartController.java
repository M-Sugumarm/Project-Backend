package com.whitehouse.controller;

import com.whitehouse.model.CartItem;
import com.whitehouse.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable String userId) {
        return cartService.getCartItems(userId);
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(@RequestBody Map<String, Object> request) {
        String userId = request.get("userId").toString();
        String productId = request.get("productId").toString();
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String size = request.get("size") != null ? request.get("size").toString() : null;
        String color = request.get("color") != null ? request.get("color").toString() : null;

        CartItem item = cartService.addToCart(userId, productId, quantity, size, color);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable String itemId,
            @RequestBody Map<String, Integer> request) {
        CartItem item = cartService.updateCartItem(itemId, request.get("quantity"));
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String itemId) {
        cartService.removeFromCart(itemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
