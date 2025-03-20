package com.example.budgetbuddy;

public class Transaction {
    private String type, category, note, date;
    private double amount;

    public Transaction() {}
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getDate() { return date; }
}