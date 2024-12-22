package com.alp2app.chatuygulamasi;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "messages")
public class Message {

    @PrimaryKey
    @NonNull
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    private String type;
    private String mediaUrl;
    private boolean seen;
    private String chatId;

    public Message() {
        this.messageId = UUID.randomUUID().toString();
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        if (content == null) {
            return "";
        }
        return content;
    }

    public void setContent(String content) {
        if (content == null) {
            content = "";
        }
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageId.equals(message.messageId) &&
                Objects.equals(content, message.content) &&
                Objects.equals(senderId, message.senderId) &&
                Objects.equals(receiverId, message.receiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, senderId, receiverId, content, timestamp, type, mediaUrl, seen, chatId);
    }

    public static String generateChatId(String userId1, String userId2) {
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }
}
