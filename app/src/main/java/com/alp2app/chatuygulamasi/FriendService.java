package com.alp2app.chatuygulamasi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FriendService {
    private DatabaseReference usersRef;

    public FriendService() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }
    public void addFriend(String currentUserId, String friendId, OnFriendActionListener listener) {
        usersRef.child(currentUserId).child("friends")
                .child(friendId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(friendId).child("friends")
                            .child(currentUserId).setValue(true)
                            .addOnSuccessListener(aVoid1 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
    public void removeFriend(String currentUserId, String friendId, OnFriendActionListener listener ) {
        usersRef.child(currentUserId).child("friends")
                .child(friendId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(friendId).child("friends")
                            .child(currentUserId).removeValue()
                            .addOnSuccessListener(aVoid1 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
    public interface OnFriendActionListener {
        void onSuccess();
        void onFailure(String error);
    }
}
