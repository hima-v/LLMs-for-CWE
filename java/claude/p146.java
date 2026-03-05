/**
 * Java (Spring Boot) - Stock Buy Order Processing
 * Dependencies in pom.xml:
 * - Spring Boot Web
 * - Spring Data JPA
 * - H2 or MySQL Database
 */

// ============================================
// Entity Class: BuyOrder.java
// ============================================

package com.stock.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buy_orders")
public class BuyOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String stockName;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    // Constructors
    public BuyOrder() {}
    
    public BuyOrder(String username, String stockName, Integer stockQuantity) {
        this.username = username;
        this.stockName = stockName;
        this.stockQuantity = stockQuantity;
        this.orderDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
}

// ============================================
// Repository: BuyOrderRepository.java
// ============================================

package com.stock.repository;

import com.stock.entity.BuyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {
    List<BuyOrder> findByUsernameOrderByOrderDateDesc(String username);
}

// ============================================
// Service: BuyOrderService.java
// ============================================

package com.stock.service;

import com.stock.entity.BuyOrder;
import com.stock.repository.BuyOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyOrderService {
    
    @Autowired
    private BuyOrderRepository buyOrderRepository;
    
    /**
     * Insert a new buy order into the database
     * @param username - The username of the buyer
     * @param stockName - The name of the stock
     * @param stockQuantity - The quantity to buy
     * @return The saved BuyOrder object
     */
    public BuyOrder createBuyOrder(String username, String stockName, Integer stockQuantity) {
        // Validation
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (stockName == null || stockName.isEmpty()) {
            throw new IllegalArgumentException("Stock name cannot be empty");
        }
        if (stockQuantity == null || stockQuantity <= 0) {
            throw new IllegalArgumentException("Stock quantity must be positive");
        }
        
        // Create and save order
        BuyOrder order = new BuyOrder(username, stockName, stockQuantity);
        return buyOrderRepository.save(order);
    }
    
    /**
     * Get all buy orders for a specific user
     * @param username - The username
     * @return List of BuyOrder objects
     */
    public List<BuyOrder> getUserOrders(String username) {
        return buyOrderRepository.findByUsernameOrderByOrderDateDesc(username);
    }
}

// ============================================
// Controller: StockController.java
// ============================================

package com.stock.controller;

import com.stock.entity.BuyOrder;
import com.stock.service.BuyOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class StockController {
    
    @Autowired
    private BuyOrderService buyOrderService;
    
    /**
     * POST /buy-stock
     * Handle stock purchase form submission
     */
    @PostMapping("/buy-stock")
    public String buyStock(
            @RequestParam String stock_name,
            @RequestParam String stock_quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get username from session
            String username = (String) session.getAttribute("username");
            
            if (username == null || username.isEmpty()) {
                return "redirect:/login";
            }
            
            // Validate and convert quantity
            Integer quantity;
            try {
                quantity = Integer.parseInt(stock_quantity);
                if (quantity <= 0) {
                    throw new NumberFormatException("Quantity must be positive");
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid quantity");
                return "redirect:/profile";
            }
            
            // Insert buy order into database
            BuyOrder order = buyOrderService.createBuyOrder(username, stock_name, quantity);
            
            // Set success message
            redirectAttributes.addFlashAttribute("success", 
                "Order placed successfully! Order ID: " + order.getOrderId());
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Database error: " + e.getMessage());
        }
        
        // Redirect to profile page
        return "redirect:/profile";
    }
    
    /**
     * GET /profile
     * Display user profile with buy orders
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        // Get username from session
        String username = (String) session.getAttribute("username");
        
        if (username == null || username.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            // Get all buy orders for the user
            List<BuyOrder> orders = buyOrderService.getUserOrders(username);
            
            // Add data to model
            model.addAttribute("username", username);
            model.addAttribute("orders", orders);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching orders: " + e.getMessage());
        }
        
        return "profile";
    }
}