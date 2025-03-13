package com.example.budgetbuddy;

public class Expense {
    private String price;
    private String category;
    private String date;

    // Default constructor required for Firestore
    public Expense() {}

    public Expense(String price, String category, String date) {
        this.price = price;
        this.category = category;
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }
}