package com.example.budgetbuddy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

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

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        Button btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());
        setupRecyclerView();
        loadTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("transactions")
                .whereEqualTo("type", "Expense")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    transactions.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setId(doc.getId());
                        transactions.add(transaction);
                    }
                    adapter.notifyDataSetChanged();
                    if (transactions.isEmpty()) {
                        Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}