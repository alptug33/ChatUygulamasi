package com.alp2app.chatuygulamasi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class NotificationService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "chat_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        sendNotification(title,body);
    }
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Yeni mesaj bildirimleri");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private void sendNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(generateNotificationId(),builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateTokenOnServer(token);
    }
    private void updateTokenOnServer(String token) {
        //TODO : TOKENI FİREBASES VERİTABANINDA GÜNCELLE
    }
}
