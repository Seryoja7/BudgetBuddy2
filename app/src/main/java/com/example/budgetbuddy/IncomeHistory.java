package com.example.budgetbuddy;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IncomeHistory extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        loadTransactions("Income");
    }

    private void loadTransactions(String type) {
        db.collection("transactions")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = queryDocumentSnapshots.toObjects(Transaction.class);
                    recyclerView.setAdapter(new TransactionAdapter(transactions));
                });
    }
}