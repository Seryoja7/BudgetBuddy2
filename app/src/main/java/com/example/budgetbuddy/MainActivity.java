package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the "Add Expense" button
        Button btnAddExpense = findViewById(R.id.btnAddExpense);

        // Set click listener for the "Add Expense" button
        btnAddExpense.setOnClickListener(v -> {
            // Navigate to AddExpenseActivity
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });
    }
}