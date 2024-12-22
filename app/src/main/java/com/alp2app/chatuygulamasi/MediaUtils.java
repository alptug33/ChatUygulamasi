package com.alp2app.chatuygulamasi;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class MediaUtils {
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public MediaUtils() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }
    public void uploadMedia(Uri mediaUri, String mediaType,OnMediaUploadListener listener) {
        String fileName = UUID.randomUUID().toString();
        StorageReference mediaRef = storageRef.child("media/" + fileName);

        mediaRef.putFile(mediaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    mediaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString(),mediaType);
                    });
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnMediaUploadListener {
        void onSuccess(String mediaUrl, String mediaType);
        void onFailure(String error);
    }
}
