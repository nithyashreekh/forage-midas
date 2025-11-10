package com.jpmc.midascore;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Simple User entity representing an account with a balance.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // a human-readable name (e.g. "waldorf")
    @Column(nullable = false, unique = true)
    private String name;

    // use BigDecimal for money to avoid floating-point errors
    @Column(nullable = false)
    private BigDecimal balance;

    // JPA requires a no-arg constructor
    public User() { }

    public User(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Decrease the user's balance by amount (assumes caller validated sufficient funds).
     */
    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Increase the user's balance by amount.
     */
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
