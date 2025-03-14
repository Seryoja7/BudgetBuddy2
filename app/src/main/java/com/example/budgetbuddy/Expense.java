package com.example.budgetbuddy;

public class Expense {
    private String title;
    private String price;
    private String category;
    private String paymentMethod;
    private String currency;
    private String notes;
    private String date;

    // Default constructor required for Firestore
    public Expense() {
    }

    public Expense(String title, String price, String category, String paymentMethod, String currency, String notes, String date) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.notes = notes;
        this.date = date;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}