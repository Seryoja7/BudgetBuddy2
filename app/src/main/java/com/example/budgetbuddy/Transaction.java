package com.example.budgetbuddy;

import com.google.firebase.Timestamp;

public class Transaction {
    private String id;
    private String type;
    private String category;
    private double amount;
    private String note;
    private Timestamp date; // Use Firestore Timestamp

    // Default constructor (required for Firestore)
    public Transaction() {}

    // Parameterized constructor (optional)
    public Transaction(String type, String category, double amount, String note, Timestamp date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.date = date;
    }

    // Getters and setters (required for Firestore)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}