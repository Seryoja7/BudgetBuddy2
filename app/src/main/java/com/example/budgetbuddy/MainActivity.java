package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnViewExpenses = findViewById(R.id.btnViewExpenses);
        Button btnBack = findViewById(R.id.btnBack);

        // Add Expense button click
        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        // View Expenses button click
        btnViewExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewExpensesActivity.class);
            startActivity(intent);
        });

        // Back button click (with sign-out)
        btnBack.setOnClickListener(v -> {
            Log.d("MainActivity", "Back button clicked");

            // Sign out the user and navigate back to Login
            FirebaseAuth.getInstance().signOut(); // Clear Firebase auth state
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish(); // Close MainActivity
        });
    }
}