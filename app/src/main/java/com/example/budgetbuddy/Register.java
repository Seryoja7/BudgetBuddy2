package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
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
    boolean isRegistrationInProgress = false;

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

        // Navigate to Login screen
        textView.setOnClickListener(v -> {
            if (tempUser != null && !tempUser.isEmailVerified()) {
                deleteTempUserAndNavigateToLogin();
            } else {
                navigateToLogin();
            }
        });

        // Handle registration
        buttonReg.setOnClickListener(v -> {
            if (isRegistrationInProgress) {
                return;
            }

            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Մուտքագրեք էլ. փոստը", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Register.this, "Անվավեր էլ. փոստ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Register.this, "Գաղտնաբառը պետք է լինի առնվազն 6 նիշ", Toast.LENGTH_SHORT).show();
                return;
            }

            isRegistrationInProgress = true;
            buttonReg.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tempUser = mAuth.getCurrentUser();
                            if (tempUser != null) {
                                sendVerificationEmail(tempUser);
                            } else {
                                Toast.makeText(Register.this, "Սխալ: Օգտատերը ստեղծված չէ", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                buttonReg.setEnabled(true);
                                isRegistrationInProgress = false;
                            }
                        } else {
                            Toast.makeText(Register.this, "Գրանցում ձախողվեց: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            buttonReg.setEnabled(true);
                            isRegistrationInProgress = false;
                        }
                    });
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(sendTask -> {
                    if (sendTask.isSuccessful()) {
                        Toast.makeText(Register.this, "Հաստատման նամակը ուղարկված է " + user.getEmail(), Toast.LENGTH_LONG).show();
                        startVerificationCheck(user);
                    } else {
                        Exception exception = sendTask.getException();
                        Log.e("Register", "Email send failed", exception);
                        Toast.makeText(Register.this, "Նամակի ուղարկումը ձախողվեց: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        buttonReg.setEnabled(true);
                        isRegistrationInProgress = false;
                    }
                });
    }

    private void startVerificationCheck(FirebaseUser user) {
        handler = new Handler();
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                user.reload()
                        .addOnCompleteListener(reloadTask -> {
                            if (reloadTask.isSuccessful()) {
                                if (user.isEmailVerified()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Register.this, "Էլ. փոստը հաստատված է", Toast.LENGTH_SHORT).show();
                                    navigateToLogin();
                                } else {
                                    handler.postDelayed(this, 5000); // Check every 5 seconds
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Register.this, "Սխալ: " + reloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                buttonReg.setEnabled(true);
                                isRegistrationInProgress = false;
                            }
                        });
            }
        };
        handler.post(verificationChecker);
    }

    private void deleteTempUserAndNavigateToLogin() {
        if (tempUser != null) {
            tempUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "Օգտատերը ջնջվեց", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Ջնջումը ձախողվեց", Toast.LENGTH_SHORT).show();
                        }
                        navigateToLogin();
                    });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
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