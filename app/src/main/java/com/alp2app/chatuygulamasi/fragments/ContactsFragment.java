package com.alp2app.chatuygulamasi.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alp2app.chatuygulamasi.R;
import com.alp2app.chatuygulamasi.UsersAdapter;
import com.alp2app.chatuygulamasi.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ContactsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        
        adapter = new UsersAdapter(new ArrayList<>(), userId -> {
            // Kişi tıklama işlemi
            startChat(userId);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadContacts);
        
        loadContacts();
        return view;
    }

    private void loadContacts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        users.add(user);
                    }
                }
                adapter.updateList(users);
            }
            swipeRefresh.setRefreshing(false);
        });
    }

    private void startChat(String userId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        
        String chatId = chatsRef.push().getKey();
        if (chatId != null) {
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("participants/" + currentUserId, true);
            chatData.put("participants/" + userId, true);
            chatData.put("createdAt", System.currentTimeMillis());
            
            chatsRef.child(chatId).setValue(chatData);
        }
    }
} 