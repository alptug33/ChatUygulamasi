package com.alp2app.chatuygulamasi;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChatActivity extends AppCompatActivity implements UsersAdapter.OnUserClickListener {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        // Toolbar ayarları
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Yeni Sohbet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setupRecyclerView();
        loadAllUsers();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.usersRecyclerView);
        adapter = new UsersAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadAllUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        users.add(user);
                    }
                }
                adapter.updateList(users);
            } else {
                Toast.makeText(this, "Kullanıcılar yüklenemedi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(String userId) {
        // Yeni sohbet oluştur
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        String chatId = chatsRef.push().getKey();
        
        if (chatId != null) {
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("participants/" + currentUserId, true);
            chatData.put("participants/" + userId, true);
            chatData.put("createdAt", System.currentTimeMillis());

            chatsRef.child(chatId).setValue(chatData)
                .addOnSuccessListener(aVoid -> {
                    finish(); // NewChatActivity'yi kapat
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Sohbet oluşturulamadı", Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 