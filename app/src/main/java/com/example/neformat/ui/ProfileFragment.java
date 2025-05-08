package com.example.neformat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.neformat.R;
import com.example.neformat.ui.auth.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private TextView usernameTextView;
    private ImageView profileImageView;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameTextView = view.findViewById(R.id.usernameTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        logoutButton = view.findViewById(R.id.logoutButton);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Показываем кеш сразу
        SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        String cachedName = prefs.getString("name", null);
        String cachedAvatar = prefs.getString("avatar", null);

        if (cachedName != null) {
            usernameTextView.setText(cachedName.toUpperCase());
        }

        if (cachedAvatar != null && !cachedAvatar.isEmpty()) {
            Glide.with(requireContext())
                    .load(cachedAvatar)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.default_avatar);
        }

        // Обновляем из Firebase и сохраняем в кеш
        if (currentUser != null) {
            String uid = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avatar").getValue(String.class);

                    if (name != null) {
                        usernameTextView.setText(name.toUpperCase());
                        prefs.edit().putString("name", name).apply();
                    }

                    if (avatar != null && !avatar.isEmpty()) {
                        Glide.with(requireContext())
                                .load(avatar)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(profileImageView);
                        prefs.edit().putString("avatar", avatar).apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    usernameTextView.setText("Ошибка загрузки имени");
                }
            });
        }

        // Переход в редактирование профиля
        Button editProfileButton = view.findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new EditProfileFragment());
            transaction.addToBackStack("profile"); // добавляем в стек
            transaction.commit();
        });

        // Выход
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), AuthActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}
