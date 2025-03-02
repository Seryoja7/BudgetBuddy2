package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    Handler handler;
    Runnable verificationChecker;
    FirebaseUser tempUser;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(v -> {
            if (tempUser != null && !tempUser.isEmailVerified()) {
                deleteTempUserAndNavigateToLogin();
            } else {
                navigateToLogin();
            }
        });

        buttonReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Մուտքագրեք էլ. փոստի հասցեն", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Մուտքագրեք գաղտնաբառը", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tempUser = mAuth.getCurrentUser();
                            if (tempUser != null) {
                                sendEmailVerification(tempUser);
                                startEmailVerificationCheck(tempUser);
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "Գրանցումը ձախողվեց: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Հաստատման էլ. նամակը ուղարկվել է " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Չհաջողվեց ուղարկել հաստատման էլ. նամակը: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startEmailVerificationCheck(FirebaseUser user) {
        handler = new Handler();
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (user.isEmailVerified()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "Էլ. փոստը հաստատված է: Դուք կարող եք մուտք գործել", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            handler.postDelayed(this, 5000);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Չհաջողվեց ստուգել էլ. փոստի հաստատումը: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        handler.post(verificationChecker);
    }

    private void deleteTempUserAndNavigateToLogin() {
        if (tempUser != null) {
            tempUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Register.this, "Չհաստատված օգտատերը ջնջվեց", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Register.this, "Չհաջողվեց ջնջել օգտատիրոջը: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                navigateToLogin();
            });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && verificationChecker != null) {
            handler.removeCallbacks(verificationChecker);
        }
    }
}