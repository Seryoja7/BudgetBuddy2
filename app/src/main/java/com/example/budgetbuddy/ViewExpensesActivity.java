package com.example.budgetbuddy;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;

public class ViewExpensesActivity extends AppCompatActivity {

    private EditText etSelectDate, etCategory, etPaymentMethod;
    private Button btnView, btnBack;
    private TextView tvExpenseDetails; // TextView to display expense details
    private FirebaseFirestore db;
    private static final String TAG = "ViewExpensesActivity";

    // Predefined options for category and payment method
    private final String[] categories = {"All", "Food", "Transportation", "Rent", "Entertainment", "Utilities", "Other"};
    private final String[] paymentMethods = {"All", "Cash", "Credit Card", "Debit Card", "Online Payment", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // Initialize views
        etSelectDate = findViewById(R.id.etSelectDate);
        etCategory = findViewById(R.id.etCategory);
        etPaymentMethod = findViewById(R.id.etPaymentMethod);
        btnView = findViewById(R.id.btnView);
        btnBack = findViewById(R.id.btnBack); // Initialize the Back button
        tvExpenseDetails = findViewById(R.id.tvExpenseDetails); // Initialize TextView

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Date Picker
        etSelectDate.setOnClickListener(v -> showDatePicker());

        // Category Selection
        etCategory.setOnClickListener(v -> showSelectionDialog("Select Category", categories, etCategory));

        // Payment Method Selection
        etPaymentMethod.setOnClickListener(v -> showSelectionDialog("Select Payment Method", paymentMethods, etPaymentMethod));

        // View Expenses
        btnView.setOnClickListener(v -> {
            String selectedDate = etSelectDate.getText().toString().trim();
            String selectedCategory = etCategory.getText().toString().trim();
            String selectedPaymentMethod = etPaymentMethod.getText().toString().trim();

            // Fetch expenses based on selected filters
            fetchExpenses(selectedDate, selectedCategory, selectedPaymentMethod);
        });

        // Back Button
        btnBack.setOnClickListener(v -> {
            // Navigate back to MainActivity
            finish();
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

    private void showSelectionDialog(String title, String[] items, EditText targetEditText) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_selection);

        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        ListView listView = dialog.findViewById(R.id.listView);

        dialogTitle.setText(title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = items[position];
            targetEditText.setText(selectedItem);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchExpenses(String selectedDate, String selectedCategory, String selectedPaymentMethod) {
        // Start building the Firestore query
        Query query = db.collection("expenses");

        // Apply date filter if a date is selected
        if (!selectedDate.isEmpty()) {
            query = query.whereEqualTo("date", selectedDate);
        }

        // Apply category filter if a category is selected (and not "All")
        if (!selectedCategory.isEmpty() && !selectedCategory.equals("All")) {
            query = query.whereEqualTo("category", selectedCategory);
        }

        // Apply payment method filter if a payment method is selected (and not "All")
        if (!selectedPaymentMethod.isEmpty() && !selectedPaymentMethod.equals("All")) {
            query = query.whereEqualTo("paymentMethod", selectedPaymentMethod);
        }

        // Execute the query
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder expenseDetails = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Map Firestore document to Expense object
                            String title = document.getString("title");
                            String price = document.getString("price");
                            String category = document.getString("category");
                            String paymentMethod = document.getString("paymentMethod");
                            String currency = document.getString("currency");
                            String notes = document.getString("notes");
                            String date = document.getString("date");

                            // Append expense details in the desired syntax
                            expenseDetails.append("Title - ").append(title).append("\n")
                                    .append("Price - ").append(price).append("\n")
                                    .append("Category - ").append(category).append("\n")
                                    .append("Payment Method - ").append(paymentMethod).append("\n")
                                    .append("Currency - ").append(currency).append("\n")
                                    .append("Notes - ").append(notes).append("\n")
                                    .append("Date - ").append(date).append("\n\n");
                        }

                        // Display the expense details in the TextView
                        if (expenseDetails.length() > 0) {
                            tvExpenseDetails.setText(expenseDetails.toString());
                        } else {
                            tvExpenseDetails.setText("No expenses found for the selected filters.");
                        }
                    } else {
                        Log.e(TAG, "Error fetching expenses", task.getException());
                        Toast.makeText(this, "Error fetching expenses", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}