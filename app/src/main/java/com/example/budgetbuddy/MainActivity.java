package com.example.budgetbuddy;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnExpense, btnIncome;
    private ImageButton btnTransport, btnFood, btnPurchases, btnEntertainment, btnEatOutside, btnOther;
    private ImageButton btnOptions;
    private float initialXExpense, initialYExpense, initialXIncome, initialYIncome;
    private static final float SNAP_THRESHOLD = 200f;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupButtonPositions();
        setTouchListeners();

        btnOptions.setOnClickListener(v -> showOptionsDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_expense) {
            startActivity(new Intent(this, ActivityHistory.class));
            return true;
        } else if (id == R.id.action_view_income) {
            startActivity(new Intent(this, IncomeHistory.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        btnTransport = findViewById(R.id.btnTransport);
        btnFood = findViewById(R.id.btnFood);
        btnPurchases = findViewById(R.id.btnPurchases);
        btnEntertainment = findViewById(R.id.btnEntertainment);
        btnEatOutside = findViewById(R.id.btnEatOutside);
        btnOther = findViewById(R.id.btnOther);
        btnOptions = findViewById(R.id.btnOptions);
    }

    private void setupButtonPositions() {
        btnExpense.post(() -> {
            initialXExpense = btnExpense.getX();
            initialYExpense = btnExpense.getY();
        });
        btnIncome.post(() -> {
            initialXIncome = btnIncome.getX();
            initialYIncome = btnIncome.getY();
        });
    }

    private void setTouchListeners() {
        btnExpense.setOnTouchListener(new DraggableButtonListener());
        btnIncome.setOnTouchListener(new DraggableButtonListener());
    }

    private class DraggableButtonListener implements View.OnTouchListener {
        private float dX, dY;
        private boolean isDragged = false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.bringToFront();
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    isDragged = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    isDragged = true;
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isDragged) {
                        ImageButton targetCategory = findNearestCategory(view);
                        if (targetCategory != null) {
                            showInputDialog(targetCategory, view.getId() == R.id.btnExpense);
                        }
                        resetButtonPosition(view);
                    }
                    return true;
                default:
                    return false;
            }
        }

        private ImageButton findNearestCategory(View draggedView) {
            ImageButton[] categories = {
                    btnTransport, btnFood, btnPurchases,
                    btnEntertainment, btnEatOutside, btnOther
            };
            Point draggedCenter = getViewCenter(draggedView);
            ImageButton nearestCategory = null;
            float minDistance = Float.MAX_VALUE;
            for (ImageButton category : categories) {
                Point categoryCenter = getViewCenter(category);
                float distance = calculateDistance(draggedCenter, categoryCenter);
                if (distance < SNAP_THRESHOLD && distance < minDistance) {
                    minDistance = distance;
                    nearestCategory = category;
                }
            }
            return nearestCategory;
        }

        private Point getViewCenter(View view) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            return new Point(
                    location[0] + view.getWidth() / 2,
                    location[1] + view.getHeight() / 2
            );
        }

        private float calculateDistance(Point p1, Point p2) {
            return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        }

        private void resetButtonPosition(View view) {
            float targetX = view.getId() == R.id.btnExpense ? initialXExpense : initialXIncome;
            float targetY = view.getId() == R.id.btnExpense ? initialYExpense : initialYIncome;
            view.animate()
                    .x(targetX)
                    .y(targetY)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void showInputDialog(ImageButton category, boolean isExpense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        setupNumberPad(dialogView, etAmount);
        builder.setView(dialogView)
                .setTitle("Add " + (isExpense ? "Expense" : "Income") + " to " + category.getContentDescription())
                .setPositiveButton("Save", (dialog, which) -> {
                    String amount = etAmount.getText().toString();
                    String note = etNote.getText().toString();
                    if (!amount.isEmpty()) {
                        saveToFirestore(category.getContentDescription().toString(), amount, note, isExpense);
                    } else {
                        Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveToFirestore(String category, String amount, String note, boolean isExpense) {
        String type = isExpense ? "Expense" : "Income";
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("category", category);
        data.put("amount", Double.parseDouble(amount));
        data.put("note", note.isEmpty() ? "No note" : note);
        data.put("date", date);
        db.collection("transactions")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, type + " saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNumberPad(View dialogView, EditText etAmount) {
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int buttonId : numberButtons) {
            dialogView.findViewById(buttonId).setOnClickListener(v -> {
                Button btn = (Button) v;
                etAmount.append(btn.getText());
            });
        }
        dialogView.findViewById(R.id.btnDot).setOnClickListener(v -> {
            String current = etAmount.getText().toString();
            if (!current.contains(".")) {
                etAmount.append(".");
            }
        });
        dialogView.findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            String current = etAmount.getText().toString();
            if (current.length() > 0) {
                etAmount.setText(current.substring(0, current.length() - 1));
            }
        });
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);

        Button btnViewExpense = createDialogButton("View Expense", ActivityHistory.class);
        Button btnViewIncome = createDialogButton("View Income", IncomeHistory.class);
        Button btnLogout = createLogoutButton();

        layout.addView(btnViewExpense);
        layout.addView(btnViewIncome);
        layout.addView(btnLogout);

        builder.setView(layout);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.TOP | Gravity.START);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.x = 60;
            layoutParams.y = 120;
            dialog.getWindow().setAttributes(layoutParams);
        }

        btnViewExpense.setTag(dialog);
        btnViewIncome.setTag(dialog);
        btnLogout.setTag(dialog);
        dialog.show();
    }

    private Button createDialogButton(String text, Class<?> targetActivity) {
        Button button = new Button(this);
        button.setText(text);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        button.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, targetActivity));
            ((AlertDialog) v.getTag()).dismiss();
        });
        return button;
    }

    private Button createLogoutButton() {
        Button btnLogout = new Button(this);
        btnLogout.setText("Logout");
        btnLogout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finishAffinity();
            ((AlertDialog) v.getTag()).dismiss();
        });
        return btnLogout;
    }
}