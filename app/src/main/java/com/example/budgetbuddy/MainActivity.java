package com.example.budgetbuddy;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnExpense, btnIncome;
    private float initialXExpense, initialYExpense, initialXIncome, initialYIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);

        btnExpense.post(() -> {
            initialXExpense = btnExpense.getX();
            initialYExpense = btnExpense.getY();
        });

        btnIncome.post(() -> {
            initialXIncome = btnIncome.getX();
            initialYIncome = btnIncome.getY();
        });

        btnExpense.setOnTouchListener(new DragTouchListener());
        btnIncome.setOnTouchListener(new DragTouchListener());
    }

    private class DragTouchListener implements View.OnTouchListener {
        private float dX, dY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.bringToFront();
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                    if (view.getId() == R.id.btnExpense) {
                        view.animate()
                                .x(initialXExpense)
                                .y(initialYExpense)
                                .setDuration(300)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    } else if (view.getId() == R.id.btnIncome) {
                        view.animate()
                                .x(initialXIncome)
                                .y(initialYIncome)
                                .setDuration(300)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    }
                    break;
            }
            return true;
        }
    }
}