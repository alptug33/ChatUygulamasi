package com.alp2app.chatuygulamasi;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    entities = {Message.class},
    version = 3,
    exportSchema = false
)
public abstract class ChatDatabase extends RoomDatabase {
    private static ChatDatabase instance;

    public abstract MessageDao messageDao();

    public static synchronized ChatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ChatDatabase.class,
                    "chat_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
