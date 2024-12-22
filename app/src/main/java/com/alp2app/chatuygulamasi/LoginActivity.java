package com.alp2app.chatuygulamasi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(this, UsersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        Toast.makeText(this, "Giriş yapılıyor...", Toast.LENGTH_SHORT).show();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Intent intent = new Intent(LoginActivity.this, UsersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, 
                    "Giriş başarısız: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Lütfen geçerli bir e-posta adresi girin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                    
                    User newUser = new User(userId, email);
                    Log.d("LoginActivity", "Yeni kullanıcı oluşturuluyor: " + newUser.getEmail());
                    
                    userRef.setValue(newUser)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LoginActivity", "Kullanıcı veritabanına kaydedildi");
                            startActivity(new Intent(LoginActivity.this, UsersActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LoginActivity", "Kullanıcı kaydedilemedi", e);
                            Toast.makeText(LoginActivity.this, 
                                "Kullanıcı profili oluşturulamadı: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Bu email adresi zaten kayıtlı", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = "Kayıt başarısız: " + e.getMessage();
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    Log.e("LoginActivity", "Kayıt hatası: ", e);
                });
    }
} 