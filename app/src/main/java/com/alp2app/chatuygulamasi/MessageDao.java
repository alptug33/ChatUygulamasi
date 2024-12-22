package com.alp2app.chatuygulamasi;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);

    @Query("SELECT * FROM messages WHERE (senderId = :senderId AND receiverId = :receiverId) OR (senderId = :receiverId AND receiverId = :senderId) ORDER BY timestamp ASC")
    LiveData<List<Message>> getChatMessages(String senderId, String receiverId);

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    void deleteMessage(String messageId);
}
