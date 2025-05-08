package com.example.neformat.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.neformat.R;
import com.example.neformat.ui.auth.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;

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

        if (currentUser != null) {
            String uid = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            usersRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        usernameTextView.setText(name.toUpperCase());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    usernameTextView.setText("Ошибка загрузки имени");
                }
            });

            // Аватарка — статичная из ресурсов (например, default_avatar.png)
            profileImageView.setImageResource(R.drawable.default_avatar);
        }

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), AuthActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}
