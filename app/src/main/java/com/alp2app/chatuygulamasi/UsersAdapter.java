package com.alp2app.chatuygulamasi;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String userId);
    }

    public UsersAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        Log.d("UsersAdapter", "Binding user: " + user.getEmail());
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImage;
        private TextView nameText;
        private TextView emailText;

        UserViewHolder(View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(userList.get(position).getUserId());
                }
            });
        }

        void bind(User user) {
            String displayName = user.getDisplayName();
            Log.d("UsersAdapter", "Binding user with email: " + user.getEmail());
            nameText.setText(displayName != null && !displayName.isEmpty() ? displayName : user.getEmail());
            emailText.setText(user.getEmail());

            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.default_avatar)
                        .into(avatarImage);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }
        }
    }
} 