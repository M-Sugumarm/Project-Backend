package com.whitehouse.controller;

import com.whitehouse.model.User;
import com.whitehouse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/clerk/{clerkId}")
    public ResponseEntity<User> getUserByClerkId(@PathVariable String clerkId) {
        return userService.getUserByClerkId(clerkId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody Map<String, String> request) {
        String clerkId = request.get("clerkId");
        String email = request.get("email");
        String name = request.get("name");
        User user = userService.createOrUpdateUser(clerkId, email, name);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/make-admin")
    public ResponseEntity<User> makeAdmin(@PathVariable String id) {
        return ResponseEntity.ok(userService.makeAdmin(id));
    }

    @PutMapping("/{id}/revoke-admin")
    public ResponseEntity<User> revokeAdmin(@PathVariable String id) {
        return ResponseEntity.ok(userService.revokeAdmin(id));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        return ResponseEntity.ok(Map.of("total", userService.getTotalCustomers()));
    }
}
