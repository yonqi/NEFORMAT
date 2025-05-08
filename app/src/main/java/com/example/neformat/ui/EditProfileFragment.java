package com.example.neformat.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileFragment extends Fragment {

    private EditText nameEditText, avatarUrlEditText;
    private Button saveNameButton, saveAvatarButton;

    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        avatarUrlEditText = view.findViewById(R.id.avatarUrlEditText);
        saveNameButton = view.findViewById(R.id.saveNameButton);
        saveAvatarButton = view.findViewById(R.id.saveAvatarButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            // Загрузка данных из Firebase
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avatar").getValue(String.class);

                    if (name != null) nameEditText.setText(name);
                    if (avatar != null) avatarUrlEditText.setText(avatar);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            });

            ImageButton backButton = view.findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> {
                // Возвращаем на фрагмент профиля
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ProfileFragment());
                transaction.addToBackStack("profile"); // добавляем в стек
                transaction.commit();
            });

            saveNameButton.setOnClickListener(v -> {
                String name = nameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    nameEditText.setError("Введите имя");
                } else {
                    userRef.child("name").setValue(name);
                    saveToPrefs("name", name); // кешируем
                    Toast.makeText(getContext(), "Имя обновлено", Toast.LENGTH_SHORT).show();
                }
            });

            saveAvatarButton.setOnClickListener(v -> {
                String avatarUrl = avatarUrlEditText.getText().toString().trim();
                if (TextUtils.isEmpty(avatarUrl)) {
                    avatarUrlEditText.setError("Введите ссылку или оставьте поле пустым");
                } else {
                    userRef.child("avatar").setValue(avatarUrl);
                    saveToPrefs("avatar", avatarUrl); // кешируем
                    Toast.makeText(getContext(), "Аватар обновлён", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void saveToPrefs(String key, String value) {
        SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }
}
