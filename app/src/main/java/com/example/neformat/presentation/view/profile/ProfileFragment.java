package com.example.neformat.presentation.view.profile;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.neformat.MainActivity;
import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

        if (currentUser != null) {
            String uid = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded()) return;

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

        Button editProfileButton = view.findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
        });

        logoutButton.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null);
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button confirmButton = dialogView.findViewById(R.id.confirmButton);
            Button cancelButton = dialogView.findViewById(R.id.cancelButton);

            confirmButton.setOnClickListener(vyiew -> {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), com.example.neformat.presentation.view.auth.AuthActivity.class));
                requireActivity().finish();
            });

            cancelButton.setOnClickListener(vyiew -> dialog.dismiss());

            dialog.show();
        });

        Button favoritesButton = view.findViewById(R.id.favoritesButton);
        favoritesButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFavoritesFromOutside();
            }
        });

        return view;
    }
}
