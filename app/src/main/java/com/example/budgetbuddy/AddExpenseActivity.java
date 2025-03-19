package com.example.budgetbuddy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etPrice, etTitle, etNotes, etDate, etCustomCategory, etCustomPaymentMethod;
    private Spinner spinnerCurrency, spinnerCategory, spinnerPaymentMethod;
    private Button btnSaveExpense, btnBack;
    private FirebaseFirestore db;
    private static final String TAG = "AddExpenseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize views
        etPrice = findViewById(R.id.etPrice);
        etTitle = findViewById(R.id.etTitle);
        etNotes = findViewById(R.id.etNotes);
        etDate = findViewById(R.id.etDate);
        etCustomCategory = findViewById(R.id.etCustomCategory);
        etCustomPaymentMethod = findViewById(R.id.etCustomPaymentMethod);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnBack = findViewById(R.id.btnBack);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up Spinners with predefined options
        setupSpinners();

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Save Expense
        btnSaveExpense.setOnClickListener(v -> saveExpense());

        // Back Button
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddExpenseActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: Close the current activity
        });

        // Handle Category Spinner Selection
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerCategory.getSelectedItem().toString().equals("Other")) {
                    etCustomCategory.setVisibility(View.VISIBLE); // Show custom category input
                } else {
                    etCustomCategory.setVisibility(View.GONE); // Hide custom category input
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle Payment Method Spinner Selection
        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerPaymentMethod.getSelectedItem().toString().equals("Other")) {
                    etCustomPaymentMethod.setVisibility(View.VISIBLE); // Show custom payment method input
                } else {
                    etCustomPaymentMethod.setVisibility(View.GONE); // Hide custom payment method input
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSpinners() {
        // Currencies
        String[] currencies = {"USD", "AMD", "EUR", "RUB"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);

        // Categories
        String[] categories = {"Food", "Transportation", "Rent", "Entertainment", "Utilities", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Payment Methods
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Online Payment", "Other"};
        ArrayAdapter<String> paymentMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentMethodAdapter);
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
        String title = etTitle.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String currency = spinnerCurrency.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

        // Handle custom category
        if (category.equals("Other")) {
            category = etCustomCategory.getText().toString().trim();
            if (category.isEmpty()) {
                Toast.makeText(this, "Please enter a custom category", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Handle custom payment method
        if (paymentMethod.equals("Other")) {
            paymentMethod = etCustomPaymentMethod.getText().toString().trim();
            if (paymentMethod.isEmpty()) {
                Toast.makeText(this, "Please enter a custom payment method", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (price.isEmpty() || title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> expense = new HashMap<>();
        expense.put("price", price);
        expense.put("title", title);
        expense.put("category", category);
        expense.put("paymentMethod", paymentMethod);
        expense.put("currency", currency);
        expense.put("notes", notes);
        expense.put("date", date);

        db.collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Expense saved with ID: " + documentReference.getId());
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving expense", e);
                });
    }
}