package com.alp2app.chatuygulamasi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ChatService chatService;
    private String currentUserId;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Toolbar'ı ayarla
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Sohbet edilen kullanıcının adını göster
            String friendEmail = getIntent().getStringExtra("friendEmail");
            getSupportActionBar().setTitle(friendEmail != null ? friendEmail : "Sohbet");
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
        chatService = new ChatService(this);
        friendId = getIntent().getStringExtra("friendId");
        Log.d("ChatActivity", "currentUserId: " + currentUserId + ", friendId: " + friendId);
        
        if (friendId == null) {
            friendId = "testUser123";
        }

        setupRecyclerView();
        chatService.listenForMessages(currentUserId, friendId);
        observeMessages();
        setupMessageSending();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, UsersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.messagesRecyclerView);
        adapter = new ChatAdapter(currentUserId);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
    private void setupMessageSending() {
        EditText input = findViewById(R.id.messageInput);
        findViewById(R.id.sendButton).setOnClickListener(v -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                Message message = new Message();
                message.setSenderId(currentUserId);
                message.setReceiverId(friendId);
                message.setContent(content);
                message.setTimestamp(System.currentTimeMillis());
                message.setType("text");
                message.setSeen(false);
                message.setChatId(Message.generateChatId(currentUserId, friendId));

                Log.d("ChatActivity", "Mesaj gönderiliyor: " + content);
                chatService.sendMessage(message);
                input.setText("");
            }
        });
    }
    private void observeMessages() {
        Log.d("ChatActivity", "observeMessages başladı");
        Log.d("ChatActivity", "currentUserId: " + currentUserId + ", friendId: " + friendId);
        
        ChatDatabase.getInstance(this).messageDao()
                .getChatMessages(currentUserId, friendId)
                .observe(this, messages -> {
                    Log.d("ChatActivity", "Mesaj sayısı: " + (messages != null ? messages.size() : 0));
                    if (messages != null) {
                        for (Message msg : messages) {
                            Log.d("ChatActivity", "Mesaj içeriği: " + msg.getContent());
                        }
                    }
                    adapter.submitList(messages);
                    if (messages != null && !messages.isEmpty()) {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    }
                });
    }
}