package com.whitehouse.controller;

import com.whitehouse.model.Product;
import com.whitehouse.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<Product> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        return ResponseEntity.ok(inventoryService.getInventoryStats());
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return inventoryService.getLowStockProducts(threshold);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateStock(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request) {
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String operation = request.get("operation").toString();
        Product updated = inventoryService.updateStock(productId, quantity, operation);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<String> bulkUpdateStock(@RequestBody List<Map<String, Object>> updates) {
        inventoryService.bulkUpdateStock(updates);
        return ResponseEntity.ok("Inventory updated successfully");
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        boolean available = inventoryService.checkStock(productId, quantity);
        return ResponseEntity.ok(Map.of("available", available, "productId", productId));
    }
}
