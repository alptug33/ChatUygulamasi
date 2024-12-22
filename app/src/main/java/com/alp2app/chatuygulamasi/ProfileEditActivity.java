package com.alp2app.chatuygulamasi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileEditActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private EditText displayNameInput;
    private Uri selectedImageUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .into(profileImageView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Toolbar'ı ayarla
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profili Düzenle");
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference();

        profileImageView = findViewById(R.id.profileImageView);
        displayNameInput = findViewById(R.id.displayNameInput);
        Button saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        // Mevcut profil bilgilerini yükle
        loadCurrentProfile();

        profileImageView.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        saveButton.setOnClickListener(v -> saveProfile());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCurrentProfile() {
        String displayName = currentUser.getDisplayName();
        Uri photoUrl = currentUser.getPhotoUrl();

        if (displayName != null) {
            displayNameInput.setText(displayName);
        }

        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(profileImageView);
        }
    }

    private void saveProfile() {
        String displayName = displayNameInput.getText().toString().trim();
        if (displayName.isEmpty()) {
            Toast.makeText(this, "Lütfen bir isim girin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImage(selectedImageUri);
        } else {
            updateProfile(displayName, currentUser.getPhotoUrl());
        }
    }

    private void updateProfile(String displayName, Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Firebase Realtime Database'i güncelle
                    userRef.child("displayName").setValue(displayName);
                    if (photoUri != null) {
                        userRef.child("photoUrl").setValue(photoUri.toString());
                    }
                    
                    Toast.makeText(this, "Profil güncellendi", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Profil güncellenemedi: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
    }

    private void uploadImage(Uri imageUri) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            
            // Resmi küçült
            int maxSize = 256;
            float ratio = Math.min(
                (float) maxSize / bitmap.getWidth(),
                (float) maxSize / bitmap.getHeight()
            );
            int width = Math.round(bitmap.getWidth() * ratio);
            int height = Math.round(bitmap.getHeight() * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();
            
            if (data.length > 1024 * 1024) { // 1MB limit
                Toast.makeText(this, "Resim boyutu çok büyük. Lütfen daha küçük bir resim seçin.", 
                    Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            String fileName = currentUser.getUid() + "_profile.jpg";
            StorageReference imageRef = storageRef.child(currentUser.getUid())
                .child(fileName);

            // Önce varolan resmi sil
            imageRef.delete().addOnCompleteListener(task -> {
                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                });

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        updateProfile(displayNameInput.getText().toString().trim(), Uri.parse(photoUrl));
                        progressBar.setVisibility(View.GONE);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileEditActivity.this,
                        "Resim yüklenemedi: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                    Log.e("ProfileEditActivity", "Resim yükleme hatası", e);
                });
            });
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Resim işlenirken hata oluştu", Toast.LENGTH_LONG).show();
            Log.e("ProfileEditActivity", "Resim işleme hatası", e);
        }
    }

    private boolean isImageTypeSupported(String mimeType) {
        return mimeType.equals("image/jpeg") ||
               mimeType.equals("image/jpg") ||
               mimeType.equals("image/png") ||
               mimeType.equals("image/gif");
    }
}