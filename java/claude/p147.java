/**
 * Java Spring Boot - Stock Buy Order Handler
 * Simple example of form submission and database insertion
 */

// --- Order Entity (Model) ---
package com.stock.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String stockName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public Order() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Order(String stockName, Integer quantity) {
        this.stockName = stockName;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// --- OrderRepository (Database Interface) ---
package com.stock.repository;

import com.stock.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStockNameOrderByCreatedAtDesc(String stockName);
}

// --- StockController (Route Handler) ---
package com.stock.controller;

import com.stock.model.Order;
import com.stock.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class StockController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    // Route: Display the form
    @GetMapping("/")
    public String showForm() {
        return "form";
    }
    
    // Route: Handle form submission and insert into database
    @PostMapping("/buy_order")
    public String buyOrder(
            @RequestParam String stockName,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        
        // Validate input
        if (stockName == null || stockName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Stock name is required");
            return "redirect:/";
        }
        
        if (quantity == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity must be positive");
            return "redirect:/";
        }
        
        // Create and save order to database
        Order order = new Order(stockName, quantity);
        orderRepository.save(order);
        
        // Redirect to stock view page
        return "redirect:/stock_view";
    }
    
    // Route: Display all stock orders
    @GetMapping("/stock_view")
    public String stockView(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "stock_view";
    }
}

// --- application.properties Configuration ---
/*
spring.application.name=stock-order-app
server.port=8080

# Database Configuration (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:stockdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# View Configuration
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html
*/

// --- pom.xml Dependencies ---
/*
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
</dependencies>
*/