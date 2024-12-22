package com.alp2app.chatuygulamasi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

public class ChatAdapter extends ListAdapter<Message, ChatAdapter.MessageViewHolder> {
    private final String currentUserId;

    public ChatAdapter(String currentUserId) {
        super(new DiffUtil.ItemCallback<Message>() {
            @Override
            public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.getMessageId().equals(newItem.getMessageId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = getItem(position);
        
        if (message.getType() == null || message.getType().equals("text")) {
            holder.messageText.setText(message.getContent());
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
        } else if (message.getType().equals("image")) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            try {
                byte[] imageBytes = Base64.decode(message.getMediaUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.messageImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ChatAdapter", "Resim yüklenemedi", e);
                holder.messageImage.setVisibility(View.GONE);
                holder.messageText.setVisibility(View.VISIBLE);
                holder.messageText.setText("Resim yüklenemedi");
            }
        }

        boolean isCurrentUser = message.getSenderId().equals(currentUserId);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        
        if (isCurrentUser) {
            holder.messageContainer.setBackgroundResource(R.drawable.message_background_sent);
            params.gravity = Gravity.END;
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.message_background_received);
            params.gravity = Gravity.START;
        }
        holder.messageContainer.setLayoutParams(params);

        holder.timeText.setText(formatTime(message.getTimestamp()));
        
        if (isCurrentUser && message.isSeen()) {
            holder.seenIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.seenIndicator.setVisibility(View.GONE);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        LinearLayout messageContainer;
        TextView timeText;
        ImageView seenIndicator;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            timeText = itemView.findViewById(R.id.timeText);
            seenIndicator = itemView.findViewById(R.id.seenIndicator);
        }
    }

    private String formatTime(long timestamp) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format("HH:mm", new Date(timestamp)).toString();
    }
}
