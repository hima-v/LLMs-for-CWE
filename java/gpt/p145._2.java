// Purchase.java (Entity)
package com.example.trading;

import jakarta.persistence.*;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String stockName;

    @Column(nullable=false)
    private int quantity;

    protected Purchase() {}

    public Purchase(String stockName, int quantity) {
        this.stockName = stockName;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public String getStockName() { return stockName; }
    public int getQuantity() { return quantity; }
}
