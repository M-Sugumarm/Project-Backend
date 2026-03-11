package com.whitehouse.service;

import com.whitehouse.model.Product;
import com.whitehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue();
    }

    public List<Product> getUpcomingProducts() {
        return productRepository.findByIsUpcomingTrue();
    }

    public List<Product> searchProducts(String query) {
        return productRepository.search(query);
    }

    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        
        // Send new product notification email to all users
        emailService.sendNewProductNotification(savedProduct);
        
        return savedProduct;
    }

    public Product updateProduct(String id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Store old price for discount detection
        Double oldPrice = product.getPrice();
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategoryId(productDetails.getCategoryId());
        product.setIsFeatured(productDetails.getIsFeatured());
        product.setIsUpcoming(productDetails.getIsUpcoming());
        product.setSizes(productDetails.getSizes());
        product.setColors(productDetails.getColors());
        product.setMaterial(productDetails.getMaterial());
        product.setBrand(productDetails.getBrand());

        Product updatedProduct = productRepository.save(product);
        
        // Check if price was reduced (discount applied)
        if (oldPrice != null && productDetails.getPrice() != null 
            && productDetails.getPrice() < oldPrice) {
            emailService.sendDiscountNotification(updatedProduct, oldPrice);
        }
        
        return updatedProduct;
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findByStockLessThan(threshold);
    }
}
