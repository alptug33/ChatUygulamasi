package com.alp2app.chatuygulamasi;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatService {
    private final DatabaseReference chatsRef;
    private final DatabaseReference usersRef;
    private final MutableLiveData<List<Message>> messagesLiveData;

    public ChatService(Context context) {
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public void sendMessage(Message message) {
        String chatId = Message.generateChatId(message.getSenderId(), message.getReceiverId());
        message.setChatId(chatId);
        
        DatabaseReference chatRef = chatsRef.child(chatId);
        DatabaseReference newMessageRef = chatRef.child("messages").push();
        message.setMessageId(newMessageRef.getKey());
        
        // Katılımcıları ayarla
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(message.getSenderId(), true);
        participants.put(message.getReceiverId(), true);
        chatRef.child("participants").updateChildren(new HashMap<>(participants));
        
        // Mesajı kaydet
        newMessageRef.setValue(message);
        
        // Son mesajı güncelle
        chatRef.child("lastMessage").setValue(message);
    }

    public void sendImageMessage(String receiverId, Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        String imageData = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        
        Message message = new Message();
        message.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        message.setReceiverId(receiverId);
        message.setType("image");
        message.setMediaUrl(imageData);
        message.setTimestamp(System.currentTimeMillis());
        
        sendMessage(message);
    }

    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }

    public void listenForMessages(String currentUserId, String friendId) {
        String chatId = Message.generateChatId(currentUserId, friendId);
        chatsRef.child(chatId).child("messages")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Message> messages = new ArrayList<>();
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        Message message = messageSnapshot.getValue(Message.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    messagesLiveData.setValue(messages);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatService", "Mesajlar yüklenemedi", error.toException());
                }
            });
    }
}
