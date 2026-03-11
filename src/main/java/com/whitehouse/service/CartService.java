package com.whitehouse.service;

import com.whitehouse.model.CartItem;
import com.whitehouse.model.Product;
import com.whitehouse.repository.CartItemRepository;
import com.whitehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItem addToCart(String userId, String productId, Integer quantity, String size, String color) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if item already exists in cart
        List<CartItem> userCartItems = cartItemRepository.findByUserId(userId);
        for (CartItem item : userCartItems) {
            if (item.getProductId().equals(productId) && 
                ((size == null && item.getSelectedSize() == null) || (size != null && size.equals(item.getSelectedSize()))) &&
                ((color == null && item.getSelectedColor() == null) || (color != null && color.equals(item.getSelectedColor())))) {
                // Update existing item
                item.setQuantity(item.getQuantity() + quantity);
                return cartItemRepository.save(item);
            }
        }

        // Create new cart item
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setSelectedSize(size);
        cartItem.setSelectedColor(color);
        
        return cartItemRepository.save(cartItem);
    }

    public CartItem updateCartItem(String itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void removeFromCart(String itemId) {
        cartItemRepository.deleteById(itemId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
