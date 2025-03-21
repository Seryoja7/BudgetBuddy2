package com.example.budgetbuddy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class IncomeHistory extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        Button btnBack = findViewById(R.id.btnBack);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        setupRecyclerView();

        // Load income transactions from Firestore
        loadTransactions();

        // Handle back button click
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        // Query Firestore for income transactions
        db.collection("transactions")
                .whereEqualTo("type", "Income") // Filter by type = "Income"
                .orderBy("date", Query.Direction.DESCENDING) // Sort by date (newest first)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (error != null) {
                        // Handle Firestore errors
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("IncomeHistory", "Firestore error: " + error.getMessage());
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        // No data found
                        Toast.makeText(this, "No income found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Clear existing data
                    transactions.clear();

                    // Convert Firestore documents to Transaction objects
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Transaction transaction = doc.toObject(Transaction.class);
                            transaction.setId(doc.getId()); // Set document ID
                            transactions.add(transaction);
                        } catch (RuntimeException e) {
                            // Handle conversion errors
                            Log.e("IncomeHistory", "Error converting document to Transaction: " + e.getMessage());
                        }
                    }

                    // Notify adapter of data changes
                    adapter.notifyDataSetChanged();
                });
    }
}