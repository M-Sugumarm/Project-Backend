package com.whitehouse.service;

import com.whitehouse.exception.ResourceNotFoundException;
import com.whitehouse.model.User;
import com.whitehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Optional<User> getUserByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createOrUpdateUser(String clerkId, String email, String name) {
        Optional<User> existingUser = userRepository.findByClerkId(clerkId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(email);
            user.setName(name);
            return userRepository.save(user);
        }

        User newUser = new User();
        newUser.setClerkId(clerkId);
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setIsAdmin(false);
        return userRepository.save(newUser);
    }

    public User makeAdmin(String userId) {
        User user = getUserById(userId);
        user.setIsAdmin(true);
        return userRepository.save(user);
    }

    public User revokeAdmin(String userId) {
        User user = getUserById(userId);
        user.setIsAdmin(false);
        return userRepository.save(user);
    }

    public long getTotalCustomers() {
        return userRepository.findAll().size();
    }
}
