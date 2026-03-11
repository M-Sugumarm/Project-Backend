package com.whitehouse.service;

import com.whitehouse.model.Product;
import com.whitehouse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllInventory() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockLessThan(threshold);
    }

    public Map<String, Object> getInventoryStats() {
        List<Product> allProducts = productRepository.findAll();
        
        int totalProducts = allProducts.size();
        int totalStock = allProducts.stream().mapToInt(Product::getStock).sum();
        int lowStockCount = (int) allProducts.stream().filter(p -> p.getStock() < 10).count();
        int outOfStockCount = (int) allProducts.stream().filter(p -> p.getStock() == 0).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("totalStock", totalStock);
        stats.put("lowStockCount", lowStockCount);
        stats.put("outOfStockCount", outOfStockCount);
        stats.put("averageStock", totalProducts > 0 ? totalStock / totalProducts : 0);

        return stats;
    }

    public Product updateStock(String productId, int quantity, String operation) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        int newStock;
        switch (operation.toUpperCase()) {
            case "ADD":
                newStock = product.getStock() + quantity;
                break;
            case "SUBTRACT":
                if (product.getStock() < quantity) {
                    throw new RuntimeException("Insufficient stock for " + product.getName() + 
                        ". Available: " + product.getStock() + ", Required: " + quantity);
                }
                newStock = product.getStock() - quantity;
                break;
            case "SET":
                newStock = quantity;
                break;
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }

        product.setStock(newStock);
        return productRepository.save(product);
    }

    public void bulkUpdateStock(List<Map<String, Object>> updates) {
        for (Map<String, Object> update : updates) {
            String productId = update.get("productId").toString();
            Integer quantity = Integer.valueOf(update.get("quantity").toString());
            String operation = update.get("operation").toString();
            updateStock(productId, quantity, operation);
        }
    }

    public boolean checkStock(String productId, int requiredQuantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getStock() >= requiredQuantity;
    }
}
