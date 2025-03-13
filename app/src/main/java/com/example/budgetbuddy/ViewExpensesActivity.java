package com.example.budgetbuddy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewExpensesActivity extends AppCompatActivity {

    private EditText etSelectDate;
    private Button btnView, btnBack;
    private RecyclerView rvExpenses;
    private FirebaseFirestore db;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private static final String TAG = "ViewExpensesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // Initialize views
        etSelectDate = findViewById(R.id.etSelectDate);
        btnView = findViewById(R.id.btnView);
        btnBack = findViewById(R.id.btnBack); // Initialize the Back button
        rvExpenses = findViewById(R.id.rvExpenses);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expenseList);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(expenseAdapter);

        // Date Picker
        etSelectDate.setOnClickListener(v -> showDatePicker());

        // View Expenses
        btnView.setOnClickListener(v -> {
            String selectedDate = etSelectDate.getText().toString().trim();
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            } else {
                fetchExpenses(selectedDate);
            }
        });

        // Back Button
        btnBack.setOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(ViewExpensesActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: Close the current activity
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    etSelectDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void fetchExpenses(String selectedDate) {
        db.collection("expenses")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        expenseList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Expense expense = document.toObject(Expense.class);
                            expenseList.add(expense);
                        }
                        expenseAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error fetching expenses", task.getException());
                        Toast.makeText(this, "Error fetching expenses", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}