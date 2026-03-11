package com.whitehouse.controller;

import com.whitehouse.model.Bundle;
import com.whitehouse.repository.BundleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bundles")
@CrossOrigin(origins = "*")
public class BundleController {

    @Autowired
    private BundleRepository bundleRepository;

    @GetMapping
    public List<Bundle> getAllBundles() {
        return bundleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bundle> getBundleById(@PathVariable String id) {
        return bundleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Bundle createBundle(@RequestBody Bundle bundle) {
        return bundleRepository.save(bundle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bundle> updateBundle(@PathVariable String id, @RequestBody Bundle bundle) {
        if (!bundleRepository.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bundle.setId(id);
        return ResponseEntity.ok(bundleRepository.save(bundle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable String id) {
        if (!bundleRepository.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bundleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
