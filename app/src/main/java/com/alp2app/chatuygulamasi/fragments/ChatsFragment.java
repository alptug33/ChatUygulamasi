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

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        
        adapter = new UsersAdapter(new ArrayList<>(), userId -> {
            // Sohbet tıklama işlemi
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadChats);
        
        loadChats();
        return view;
    }

    private void loadChats() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        
        chatsRef.orderByChild("participants/" + currentUserId)
            .equalTo(true)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<String> chatUserIds = new ArrayList<>();
                    for (DataSnapshot chatSnapshot : task.getResult().getChildren()) {
                        for (DataSnapshot participantSnapshot : chatSnapshot.child("participants").getChildren()) {
                            String participantId = participantSnapshot.getKey();
                            if (!participantId.equals(currentUserId)) {
                                chatUserIds.add(participantId);
                            }
                        }
                    }
                    loadUserDetails(chatUserIds);
                }
                swipeRefresh.setRefreshing(false);
            });
    }

    private void loadUserDetails(List<String> userIds) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        List<User> users = new ArrayList<>();
        
        for (String userId : userIds) {
            usersRef.child(userId).get().addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    users.add(user);
                    adapter.updateList(users);
                }
            });
        }
    }
} 