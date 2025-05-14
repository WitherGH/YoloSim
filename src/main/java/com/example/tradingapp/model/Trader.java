package com.example.tradingapp.model;

import com.example.tradingapp.datastructures.CustomLinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Trader {
    private String name;
    private final Portfolio portfolio = new Portfolio();
    
    @JsonIgnore
    private final CustomLinkedList<Trade> tradesList = new CustomLinkedList<>();
    private final DoubleProperty budget = new SimpleDoubleProperty(0.0);
    
    Trader() {
        this.name = "";
    }

    public Trader(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Portfolio getPortfolio() { return portfolio; }
    public CustomLinkedList<Trade> getTradesList() { return tradesList; }
    
    public double getBudget() { return budget.get(); }
    public void setBudget(double value) { budget.set(Math.max(0, value)); }
    public DoubleProperty budgetProperty() { return budget; }
    
    public boolean hasSufficientBudget(double cost) {
        return cost <= budget.get();
    }

    public boolean deductFromBudget(double amount) {
        if (amount <= 0) return true; 
        
        if (amount > budget.get()) {
            return false; 
        }
        budget.set(budget.get() - amount);
        return true;
    }

    public void addToBudget(double amount) {
        if (amount > 0) {
            budget.set(budget.get() + amount);
        }
    }
    
    public double calculateTotalValue() {
        return portfolio.calculateTotalBalance() + budget.get();
    }

    @Override 
    public String toString() { return name; }
}
