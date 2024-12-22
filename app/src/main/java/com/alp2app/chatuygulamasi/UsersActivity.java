package com.alp2app.chatuygulamasi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UsersActivity extends AppCompatActivity implements UsersAdapter.OnUserClickListener {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private String currentUserId;
    private SwipeRefreshLayout swipeRefresh;
    private ValueEventListener usersListener;

    static {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Persistence zaten etkin olabilir
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            setupViews();
            setupRecyclerView();
            loadUsers();
        } catch (Exception e) {
            Log.e("UsersActivity", "onCreate hatası", e);
            Toast.makeText(this, "Uygulama başlatılırken hata oluştu", Toast.LENGTH_LONG).show();
            finish();
        }
        
        // Geri tuşu davranışını ayarla
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(UsersActivity.this)
                    .setTitle("Çıkış")
                    .setMessage("Uygulamadan çıkmak istiyor musunuz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        finishAffinity(); // Tüm aktiviteleri kapat
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
            }
        });
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sohbetler");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadUsers);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Çıkış yap
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            // Profil sayfasına git
            startActivity(new Intent(this, ProfileEditActivity.class));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.users_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
        return true;
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                (user.getDisplayName() != null && 
                 user.getDisplayName().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(user);
            }
        }
        adapter.updateList(filteredList);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.usersRecyclerView);
        adapter = new UsersAdapter(userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        Log.d("UsersActivity", "RecyclerView kuruldu");
    }

    private void loadUsers() {
        swipeRefresh.setRefreshing(true);
        Log.d("UsersActivity", "Kullanıcılar yükleniyor...");
        
        try {
            // Önce mevcut kullanıcının ID'sini al
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e("UsersActivity", "Kullanıcı oturum açmamış!");
                swipeRefresh.setRefreshing(false);
                return;
            }
            currentUserId = currentUser.getUid();
            
            // Firebase referansını al
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            
            // ValueEventListener kullan
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<User> users = new ArrayList<>();
                    Log.d("UsersActivity", "Veri snapshot alındı, çocuk sayısı: " + dataSnapshot.getChildrenCount());
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                Log.d("UsersActivity", "Kullanıcı yüklendi: " + user.getEmail());
                                if (!user.getUserId().equals(currentUserId)) {
                                    users.add(user);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("UsersActivity", "Kullanıcı dönüştürme hatası", e);
                        }
                    }
                    
                    Log.d("UsersActivity", "Toplam yüklenen kullanıcı sayısı: " + users.size());
                    userList.clear();
                    userList.addAll(users);
                    adapter.updateList(users);
                    swipeRefresh.setRefreshing(false);
                    
                    if (users.isEmpty()) {
                        Toast.makeText(UsersActivity.this, 
                            "Henüz başka kullanıcı bulunmuyor", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UsersActivity", "Veritabanı hatası: " + error.getMessage());
                    Toast.makeText(UsersActivity.this,
                        "Kullanıcılar yüklenemedi: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                    swipeRefresh.setRefreshing(false);
                }
            });

        } catch (Exception e) {
            Log.e("UsersActivity", "loadUsers hatası", e);
            Toast.makeText(this, "Kullanıcılar yüklenirken hata oluştu", Toast.LENGTH_LONG).show();
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onUserClick(String userId) {
        // ChatActivity'yi başlat
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("friendId", userId);
        // Seçilen kullanıcının email'ini bul ve gönder
        for (User user : userList) {
            if (user.getUserId().equals(userId)) {
                intent.putExtra("friendEmail", user.getEmail());
                break;
            }
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Activity kapatıldığında listener'ı temizle
        if (usersListener != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .removeEventListener(usersListener);
        }
    }
} 