package com.example.budgetbuddy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etPrice, etCategory, etDate;
    private Button btnSaveExpense, btnBack;
    private FirebaseFirestore db;
    private static final String TAG = "AddExpenseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize views
        etPrice = findViewById(R.id.etPrice);
        etCategory = findViewById(R.id.etCategory);
        etDate = findViewById(R.id.etDate);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnBack = findViewById(R.id.btnBack); // Initialize the Back button

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Save Expense
        btnSaveExpense.setOnClickListener(v -> saveExpense());

        // Back Button
        btnBack.setOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(AddExpenseActivity.this, MainActivity.class);
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
                    etDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveExpense() {
        String price = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (price.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> expense = new HashMap<>();
        expense.put("price", price);
        expense.put("category", category);
        expense.put("date", date);

        db.collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Expense saved with ID: " + documentReference.getId());
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving expense", e);
                });
    }
}