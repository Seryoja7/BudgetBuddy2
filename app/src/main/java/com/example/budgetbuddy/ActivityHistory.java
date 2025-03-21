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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityHistory extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        Button btnBack = findViewById(R.id.btnBack);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        setupRecyclerView();

        // Load transactions from Firestore
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

        // Query Firestore for expenses
        db.collection("transactions")
                .whereEqualTo("type", "Expense") // Filter by type = "Expense"
                .orderBy("date", Query.Direction.DESCENDING) // Sort by date (newest first)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (error != null) {
                        // Handle Firestore errors
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("ActivityHistory", "Firestore error: " + error.getMessage());
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        // No data found
                        Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show();
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
                            Log.e("ActivityHistory", "Error converting document to Transaction: " + e.getMessage());
                        }
                    }

                    // Notify adapter of data changes
                    adapter.notifyDataSetChanged();
                });
    }
}