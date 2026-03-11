package com.whitehouse.service;

import com.whitehouse.model.Product;
import com.whitehouse.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import com.whitehouse.dto.BroadcastRequest;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Autowired
    private com.whitehouse.repository.UserRepository userRepository;

    @Value("${app.store.name:Jay Shree Textiles}")
    private String storeName;

    @Value("${app.store.url:http://localhost:5173}")
    private String storeUrl;

    @Value("${spring.mail.username:noreply@jayshree.com}")
    private String fromEmail;

    @Value("${app.admin.email:sugus7215@gmail.com}")
    private String adminEmail;

    /**
     * Send new product notification to all users AND admin
     */
    @Async
    public void sendNewProductNotification(Product product) {
        List<User> users = userService.getAllUsers();
        String subject = "🆕 New Arrival: " + product.getName() + " | " + storeName;
        
        // Always send to admin first
        try {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail(adminEmail);
            String htmlContent = buildNewProductEmail(adminUser, product);
            sendHtmlEmail(adminEmail, subject, htmlContent);
            System.out.println("✅ Email sent to admin: " + adminEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to admin: " + adminEmail + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        // Send only to subscribed users
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()
                    && !user.getEmail().equals(adminEmail)
                    && Boolean.TRUE.equals(user.getEmailSubscribed())) {
                try {
                    String htmlContent = buildNewProductEmail(user, product);
                    sendHtmlEmail(user.getEmail(), subject, htmlContent);
                } catch (Exception e) {
                    System.err.println("Failed to send email to: " + user.getEmail() + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Send discount notification to all users AND admin
     */
    @Async
    public void sendDiscountNotification(Product product, Double oldPrice) {
        List<User> users = userService.getAllUsers();
        Double price = product.getPrice() != null ? product.getPrice() : 0.0;
        Double originalPrice = oldPrice != null ? oldPrice : 0.0;
        Double savings = originalPrice - price;
        int discountPercent = originalPrice > 0 ? (int)((savings * 100.0) / originalPrice) : 0;
        
        String subject = "🔥 " + discountPercent + "% OFF: " + product.getName() + " | " + storeName;
        
        // Always send to admin first
        try {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail(adminEmail);
            String htmlContent = buildDiscountEmail(adminUser, product, originalPrice, savings, discountPercent);
            sendHtmlEmail(adminEmail, subject, htmlContent);
            System.out.println("✅ Discount email sent to admin: " + adminEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send discount email to admin: " + adminEmail + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        // Send only to subscribed users
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()
                    && !user.getEmail().equals(adminEmail)
                    && Boolean.TRUE.equals(user.getEmailSubscribed())) {
                try {
                    String htmlContent = buildDiscountEmail(user, product, originalPrice, savings, discountPercent);
                    sendHtmlEmail(user.getEmail(), subject, htmlContent);
                } catch (Exception e) {
                    System.err.println("Failed to send email to: " + user.getEmail() + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Send wishlist stock/price alert to a specific user
     */
    @Async
    public void sendWishlistNotification(User user, Product product, String triggerReason) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) return;
        
        String subject = "🔔 Update on your Wishlist Item | " + storeName;
        try {
            String htmlContent = buildWishlistEmail(user, product, triggerReason);
            sendHtmlEmail(user.getEmail(), subject, htmlContent);
            System.out.println("✅ Wishlist notification sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send wishlist email to: " + user.getEmail() + " - " + e.getMessage());
        }
    }

    /**
     * Send a welcome email when a user subscribes to offers.
     */
    @Async
    public void sendWelcomeSubscriptionEmail(String email, String name) {
        String subject = "🎁 Welcome to Exclusive Offers | " + storeName;
        String htmlContent = buildWelcomeSubscriptionEmail(name);
        try {
            sendHtmlEmail(email, subject, htmlContent);
            System.out.println("✅ Welcome subscription email sent to: " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email to: " + email + " — " + e.getMessage());
        }
    }

    /**
     * Send a broadcast offer (discount or occasion) to all subscribed users.
     */
    @Async
    public void sendBroadcastOffers(BroadcastRequest request) {
        List<User> subscribedUsers = userRepository.findAllByEmailSubscribed(true);
        System.out.println("📢 Preparing to send broadcast offer to " + subscribedUsers.size() + " subscribed users.");

        String subjectPrefix = request.getOfferType().equals("OCCASION") ? "✨ " : "🔥 ";
        String subject = subjectPrefix + request.getTitle() + " | " + storeName;

        for (User user : subscribedUsers) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    String htmlContent = buildBroadcastEmailHtml(user, request);
                    sendHtmlEmail(user.getEmail(), subject, htmlContent);
                } catch (Exception e) {
                    System.err.println("❌ Failed to send broadcast email to: " + user.getEmail() + " - " + e.getMessage());
                }
            }
        }
        System.out.println("✅ Finished processing broadcast offer.");
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        System.out.println("📧 Attempting to send email to: " + to);
        System.out.println("📧 Subject: " + subject);
        System.out.println("📧 From: " + fromEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ EMAIL ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Test email sending - for debugging purposes
     */
    public void testEmailConnection() {
        System.out.println("🔍 Testing email configuration...");
        System.out.println("📧 Mail Host: smtp.gmail.com");
        System.out.println("📧 Mail Username: " + fromEmail);
        System.out.println("📧 Admin Email: " + adminEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🔧 Test Email from Jay Shree Textiles");
            helper.setText("<h1>Email Configuration Test</h1><p>If you received this, email is working!</p>", true);
            
            mailSender.send(message);
            System.out.println("✅ TEST EMAIL SENT SUCCESSFULLY!");
        } catch (Exception e) {
            System.err.println("❌ TEST EMAIL FAILED: " + e.getClass().getName());
            System.err.println("❌ Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("❌ Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    private String buildNewProductEmail(User user, Product product) {
        String userName = user.getName() != null ? user.getName() : "Valued Customer";
        String productUrl = storeUrl + "/products/" + product.getId();
        String imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
        Double price = product.getPrice() != null ? product.getPrice() : 0.0;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 50%%, #0f3460 100%%); padding: 40px 30px; text-align: center;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;">
                                %s
                            </h1>
                            <p style="color: #c9a962; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px;">
                                PREMIUM TEXTILES
                            </p>
                        </td>
                    </tr>
                    
                    <!-- New Arrival Badge -->
                    <tr>
                        <td style="padding: 30px 30px 20px 30px; text-align: center;">
                            <span style="display: inline-block; background: linear-gradient(135deg, #c9a962 0%%, #d4af37 100%%); color: #1a1a2e; padding: 8px 24px; border-radius: 25px; font-size: 12px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase;">
                                ✨ NEW ARRIVAL ✨
                            </span>
                        </td>
                    </tr>
                    
                    <!-- Greeting -->
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <h2 style="color: #1a1a2e; margin: 0; font-size: 22px; font-weight: 600;">
                                Hello, %s! 👋
                            </h2>
                            <p style="color: #666666; margin: 15px 0 0 0; font-size: 16px; line-height: 1.6;">
                                We're thrilled to introduce our latest addition to our collection. This exclusive piece has just arrived and we thought you'd love it!
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Product Card -->
                    <tr>
                        <td style="padding: 0 30px 30px 30px;">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(145deg, #fafafa 0%%, #f0f0f0 100%%); border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
                                <!-- Product Image -->
                                <tr>
                                    <td style="padding: 20px 20px 0 20px;">
                                        <img src="%s" alt="%s" style="width: 100%%; max-height: 300px; object-fit: cover; border-radius: 12px; display: block;">
                                    </td>
                                </tr>
                                <!-- Product Details -->
                                <tr>
                                    <td style="padding: 25px;">
                                        <h3 style="color: #1a1a2e; margin: 0; font-size: 20px; font-weight: 700;">
                                            %s
                                        </h3>
                                        <p style="color: #666666; margin: 12px 0; font-size: 14px; line-height: 1.6;">
                                            %s
                                        </p>
                                        <p style="margin: 20px 0;">
                                            <span style="color: #c9a962; font-size: 28px; font-weight: 700;">
                                                ₹%.2f
                                            </span>
                                        </p>
                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 30px; font-size: 14px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; transition: all 0.3s ease;">
                                            SHOP NOW →
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="background-color: #1a1a2e; padding: 30px; text-align: center;">
                            <p style="color: #c9a962; margin: 0 0 10px 0; font-size: 16px; font-weight: 600;">
                                %s
                            </p>
                            <p style="color: #888888; margin: 0 0 20px 0; font-size: 12px;">
                                Premium Quality • Handcrafted Excellence • Since 2008
                            </p>
                            <p style="color: #666666; margin: 0; font-size: 11px;">
                                © 2026 %s. All rights reserved.<br>
                                <a href="%s" style="color: #c9a962; text-decoration: none;">Visit our store</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                storeName,
                userName,
                imageUrl,
                product.getName(),
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "Discover this exquisite piece from our premium collection.",
                price,
                productUrl,
                storeName,
                storeName,
                storeUrl
            );
    }

    private String buildDiscountEmail(User user, Product product, Double oldPrice, Double savings, int discountPercent) {
        String userName = user.getName() != null ? user.getName() : "Valued Customer";
        String productUrl = storeUrl + "/products/" + product.getId();
        String imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
        Double price = product.getPrice() != null ? product.getPrice() : 0.0;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #8B0000 0%%, #B22222 50%%, #DC143C 100%%); padding: 40px 30px; text-align: center;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;">
                                %s
                            </h1>
                            <p style="color: #ffd700; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px;">
                                SPECIAL OFFER
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Discount Badge -->
                    <tr>
                        <td style="padding: 30px 30px 20px 30px; text-align: center;">
                            <span style="display: inline-block; background: linear-gradient(135deg, #ff4757 0%%, #ff6b81 100%%); color: #ffffff; padding: 12px 30px; border-radius: 30px; font-size: 18px; font-weight: 800; letter-spacing: 1px; box-shadow: 0 4px 15px rgba(255,71,87,0.4);">
                                🔥 %d%% OFF - LIMITED TIME! 🔥
                            </span>
                        </td>
                    </tr>
                    
                    <!-- Greeting -->
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <h2 style="color: #1a1a2e; margin: 0; font-size: 22px; font-weight: 600;">
                                Great news, %s! 🎉
                            </h2>
                            <p style="color: #666666; margin: 15px 0 0 0; font-size: 16px; line-height: 1.6;">
                                The price just dropped on one of our premium products! Don't miss this exclusive deal.
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Product Card -->
                    <tr>
                        <td style="padding: 0 30px 30px 30px;">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(145deg, #fff5f5 0%%, #ffe8e8 100%%); border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border: 2px solid #ff4757;">
                                <!-- Product Image -->
                                <tr>
                                    <td style="padding: 20px 20px 0 20px; position: relative;">
                                        <img src="%s" alt="%s" style="width: 100%%; max-height: 300px; object-fit: cover; border-radius: 12px; display: block;">
                                        <!-- Sale ribbon would be here -->
                                    </td>
                                </tr>
                                <!-- Product Details -->
                                <tr>
                                    <td style="padding: 25px;">
                                        <h3 style="color: #1a1a2e; margin: 0; font-size: 20px; font-weight: 700;">
                                            %s
                                        </h3>
                                        <p style="color: #666666; margin: 12px 0; font-size: 14px; line-height: 1.6;">
                                            %s
                                        </p>
                                        
                                        <!-- Price Section -->
                                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 20px 0;">
                                            <tr>
                                                <td>
                                                    <p style="margin: 0;">
                                                        <span style="color: #999999; font-size: 18px; text-decoration: line-through;">
                                                            ₹%.2f
                                                        </span>
                                                    </p>
                                                    <p style="margin: 5px 0 0 0;">
                                                        <span style="color: #ff4757; font-size: 32px; font-weight: 800;">
                                                            ₹%.2f
                                                        </span>
                                                    </p>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="background: linear-gradient(135deg, #00b894 0%%, #00cec9 100%%); color: #ffffff; padding: 12px 20px; border-radius: 12px; display: inline-block;">
                                                        <p style="margin: 0; font-size: 12px; text-transform: uppercase; letter-spacing: 1px;">You Save</p>
                                                        <p style="margin: 5px 0 0 0; font-size: 20px; font-weight: 800;">₹%.2f</p>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #ff4757 0%%, #ff6b81 100%%); color: #ffffff; text-decoration: none; padding: 16px 50px; border-radius: 30px; font-size: 15px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; box-shadow: 0 4px 15px rgba(255,71,87,0.4);">
                                            GRAB THIS DEAL →
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Urgency Banner -->
                    <tr>
                        <td style="padding: 0 30px 30px 30px;">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #ffeaa7 0%%, #fdcb6e 100%%); border-radius: 12px; padding: 20px;">
                                <tr>
                                    <td style="text-align: center; padding: 15px;">
                                        <p style="color: #1a1a2e; margin: 0; font-size: 14px; font-weight: 600;">
                                            ⏰ This offer won't last forever! Shop now before it's gone.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="background-color: #1a1a2e; padding: 30px; text-align: center;">
                            <p style="color: #c9a962; margin: 0 0 10px 0; font-size: 16px; font-weight: 600;">
                                %s
                            </p>
                            <p style="color: #888888; margin: 0 0 20px 0; font-size: 12px;">
                                Premium Quality • Handcrafted Excellence • Since 2008
                            </p>
                            <p style="color: #666666; margin: 0; font-size: 11px;">
                                © 2026 %s. All rights reserved.<br>
                                <a href="%s" style="color: #c9a962; text-decoration: none;">Visit our store</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                storeName,
                discountPercent,
                userName,
                imageUrl,
                product.getName(),
                product.getName(),
                product.getDescription() != null ? product.getDescription() : "Discover this exquisite piece from our premium collection.",
                oldPrice,
                price,
                savings,
                productUrl,
                storeName,
                storeName,
                storeUrl
            );
    }

    private String buildWelcomeSubscriptionEmail(String name) {
        String userName = (name != null && !name.isBlank()) ? name : "Valued Customer";
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 50%%, #0f3460 100%%); padding: 50px 30px; text-align: center;">
                            <div style="font-size: 48px; margin-bottom: 16px;">🎁</div>
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;">
                                %s
                            </h1>
                            <p style="color: #c9a962; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px;">
                                EXCLUSIVE OFFERS & DEALS
                            </p>
                        </td>
                    </tr>
                    <!-- Body -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <h2 style="color: #1a1a2e; margin: 0 0 15px 0; font-size: 24px;">Welcome aboard, %s! 🎉</h2>
                            <p style="color: #555555; font-size: 16px; line-height: 1.7; margin: 0 0 25px 0;">
                                You're now part of our exclusive circle of subscribers. As a member, you'll be the <strong>first to know</strong> about:
                            </p>
                            <!-- Benefits -->
                            <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td style="padding: 12px 20px; background: #f8f4ff; border-radius: 10px; margin-bottom: 12px; border-left: 4px solid #c9a962;">
                                        <p style="margin: 0; color: #1a1a2e; font-size: 15px;">🔥 <strong>Flash Sales & Discounts</strong> — up to 50%% OFF</p>
                                    </td>
                                </tr>
                                <tr><td style="height: 10px;"></td></tr>
                                <tr>
                                    <td style="padding: 12px 20px; background: #f8f4ff; border-radius: 10px; border-left: 4px solid #c9a962;">
                                        <p style="margin: 0; color: #1a1a2e; font-size: 15px;">✨ <strong>New Arrivals</strong> — be the first to shop</p>
                                    </td>
                                </tr>
                                <tr><td style="height: 10px;"></td></tr>
                                <tr>
                                    <td style="padding: 12px 20px; background: #f8f4ff; border-radius: 10px; border-left: 4px solid #c9a962;">
                                        <p style="margin: 0; color: #1a1a2e; font-size: 15px;">🎨 <strong>Exclusive Collections</strong> — handcrafted textiles</p>
                                    </td>
                                </tr>
                            </table>
                            <!-- CTA -->
                            <div style="text-align: center; margin: 35px 0;">
                                <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); color: #ffffff; text-decoration: none; padding: 16px 50px; border-radius: 30px; font-size: 15px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase;">
                                    EXPLORE COLLECTION →
                                </a>
                            </div>
                            <p style="color: #888888; font-size: 13px; text-align: center; margin: 0;">
                                To unsubscribe at any time, simply reply with "Unsubscribe" or visit our website.
                            </p>
                        </td>
                    </tr>
                    <!-- Footer -->
                    <tr>
                        <td style="background-color: #1a1a2e; padding: 30px; text-align: center;">
                            <p style="color: #c9a962; margin: 0 0 8px 0; font-size: 16px; font-weight: 600;">%s</p>
                            <p style="color: #888888; margin: 0 0 15px 0; font-size: 12px;">Premium Quality • Handcrafted Excellence • Since 2008</p>
                            <p style="color: #666666; margin: 0; font-size: 11px;">
                                © 2026 %s. All rights reserved.<br>
                                <a href="%s" style="color: #c9a962; text-decoration: none;">Visit our store</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                storeName,
                userName,
                storeUrl,
                storeName,
                storeName,
                storeUrl
            );
    }

    private String buildBroadcastEmailHtml(User user, BroadcastRequest request) {
        String userName = (user.getName() != null && !user.getName().isBlank()) ? user.getName() : "Valued Customer";
        
        String badgeText = "EXCLUSIVE OFFER";
        if ("DISCOUNT_10".equals(request.getOfferType()) || "DISCOUNT_20".equals(request.getOfferType())) {
            badgeText = request.getDiscountPercent() + "% OFF";
        } else if ("OCCASION".equals(request.getOfferType())) {
            badgeText = "FESTIVE SPECIAL";
        }
        
        String promoHtml = "";
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            promoHtml = """
                <div style="text-align: center; margin: 35px 0;">
                    <p style="color: #666666; font-size: 13px; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 10px 0;">Use Code At Checkout</p>
                    <div style="display: inline-block; background: #ffffff; color: #1a1a2e; font-size: 24px; font-weight: 800; letter-spacing: 4px; padding: 15px 40px; border-radius: 8px; border: 2px dashed #c9a962; box-shadow: 0 4px 15px rgba(201, 169, 98, 0.15);">
                        %s
                    </div>
                </div>
                """.formatted(request.getPromoCode());
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f7f6;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.08);">
                    <!-- Header with Image Background -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #16213e 0%%, #0f3460 100%%); padding: 60px 40px; text-align: center; position: relative;">
                            <div style="display: inline-block; background: rgba(201, 169, 98, 0.15); border: 1px solid rgba(201, 169, 98, 0.4); color: #c9a962; font-size: 11px; font-weight: 700; letter-spacing: 3px; text-transform: uppercase; padding: 8px 18px; border-radius: 30px; margin-bottom: 25px;">
                                %s
                            </div>
                            <h1 style="color: #ffffff; margin: 0; font-size: 36px; line-height: 1.2; font-weight: 800; letter-spacing: -0.5px;">%s</h1>
                        </td>
                    </tr>
                    <!-- Body Content -->
                    <tr>
                        <td style="padding: 50px 40px;">
                            <h2 style="color: #1a1a2e; margin: 0 0 24px 0; font-size: 22px; font-weight: 600;">Hi %s,</h2>
                            
                            <div style="color: #4a5568; line-height: 1.8; font-size: 16px; margin: 0 0 30px 0; white-space: pre-line;">
                                %s
                            </div>
                            
                            %s
                            
                            <!-- Enhanced CTA -->
                            <div style="text-align: center; margin-top: 45px; margin-bottom: 20px;">
                                <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #c9a962 0%%, #d4af37 100%%); color: #1a1a2e; text-decoration: none; padding: 18px 45px; border-radius: 12px; font-weight: 700; font-size: 16px; letter-spacing: 1px; text-transform: uppercase; box-shadow: 0 8px 25px rgba(201, 169, 98, 0.35);">
                                    Shop Now &rarr;
                                </a>
                            </div>
                        </td>
                    </tr>
                    <!-- Beautiful Footer -->
                    <tr>
                        <td style="background-color: #0f172a; padding: 40px 30px; text-align: center;">
                            <h3 style="color: #c9a962; margin: 0 0 12px 0; font-size: 20px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase;">%s</h3>
                            <p style="color: rgba(255,255,255,0.6); margin: 0 0 20px 0; font-size: 13px; font-weight: 500; letter-spacing: 0.5px;">
                                Premium Authentic Indian Handlooms
                            </p>
                            <div style="height: 1px; background: rgba(255,255,255,0.1); width: 60%%; margin: 0 auto 20px;"></div>
                            <p style="color: rgba(255,255,255,0.4); margin: 0 0 8px 0; font-size: 11px; line-height: 1.6;">
                                You are receiving this because you're a VIP subscriber.
                            </p>
                            <p style="color: rgba(255,255,255,0.3); margin: 0; font-size: 11px;">
                                © 2026 %s. All rights reserved.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                badgeText,
                request.getTitle(),
                userName,
                request.getMessage(),
                promoHtml,
                storeUrl,
                storeName,
                storeName
            );
    }

    private String buildWishlistEmail(User user, Product product, String triggerReason) {
        String userName = (user.getName() != null && !user.getName().isBlank()) ? user.getName() : "Valued Customer";
        String productUrl = storeUrl + "/products/" + product.getId();
        String imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
        Double price = product.getPrice() != null ? product.getPrice() : 0.0;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 50%%, #0f3460 100%%); padding: 40px 30px; text-align: center;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;">
                                %s
                            </h1>
                            <p style="color: #c9a962; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px;">
                                WISHLIST UPDATE
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Alert Badge -->
                    <tr>
                        <td style="padding: 30px 30px 20px 30px; text-align: center;">
                            <span style="display: inline-block; background: linear-gradient(135deg, #ff4757 0%%, #ff6b81 100%%); color: #ffffff; padding: 8px 24px; border-radius: 25px; font-size: 14px; font-weight: 700; letter-spacing: 1px;">
                                🔔 %s
                            </span>
                        </td>
                    </tr>
                    
                    <!-- Greeting -->
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <h2 style="color: #1a1a2e; margin: 0; font-size: 20px; font-weight: 600;">
                                Hi %s,
                            </h2>
                            <p style="color: #666666; margin: 15px 0 0 0; font-size: 16px; line-height: 1.6;">
                                We're reaching out because an item you love has an important update! Don't miss out.
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Product Card -->
                    <tr>
                        <td style="padding: 0 30px 30px 30px;">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: #fafafa; border-radius: 12px; overflow: hidden; border: 1px solid #eaeaea;">
                                <!-- Product Image -->
                                <tr>
                                    <td style="padding: 20px 20px 0 20px;">
                                        <img src="%s" alt="%s" style="width: 100%%; max-height: 250px; object-fit: cover; border-radius: 8px; display: block;">
                                    </td>
                                </tr>
                                <!-- Product Details -->
                                <tr>
                                    <td style="padding: 20px;">
                                        <h3 style="color: #1a1a2e; margin: 0; font-size: 18px; font-weight: 700;">
                                            %s
                                        </h3>
                                        <p style="margin: 15px 0;">
                                            <span style="color: #c9a962; font-size: 24px; font-weight: 700;">
                                                ₹%.2f
                                            </span>
                                        </p>
                                        <a href="%s" style="display: inline-block; background: #1a1a2e; color: #ffffff; text-decoration: none; padding: 12px 35px; border-radius: 25px; font-size: 14px; font-weight: 600; text-transform: uppercase;">
                                            View in Store
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="background-color: #1a1a2e; padding: 30px; text-align: center;">
                            <p style="color: #666666; margin: 0; font-size: 11px;">
                                © 2026 %s. All rights reserved.<br>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                storeName,
                triggerReason,
                userName,
                imageUrl,
                product.getName(),
                product.getName(),
                price,
                productUrl,
                storeName
            );
    }
}
