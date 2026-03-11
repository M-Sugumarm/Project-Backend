package com.whitehouse.controller;

import com.whitehouse.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailTestController {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.admin.email:sugus7215@gmail.com}")
    private String adminEmail;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEmail() {
        Map<String, String> result = new HashMap<>();
        result.put("from", fromEmail);
        result.put("to", adminEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("Test Email from Jay Shree Textiles");
            helper.setText("<h1>Email Test</h1><p>If you received this, email is working!</p>", true);
            
            mailSender.send(message);
            
            result.put("status", "SUCCESS");
            result.put("message", "Email sent successfully to " + adminEmail);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getClass().getSimpleName());
            result.put("message", e.getMessage());
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.ok(result);
        }
    }
}
