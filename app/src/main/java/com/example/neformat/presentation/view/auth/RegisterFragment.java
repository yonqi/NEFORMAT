package com.example.neformat.presentation.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.example.neformat.MainActivity;
import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageView privacyCheckIcon; // кастомный чекбокс
    private Button registerButton;
    private TextView toLogin;

    private FirebaseAuth auth;
    private boolean isPrivacyChecked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        privacyCheckIcon = view.findViewById(R.id.customCheckbox); // ImageView
        registerButton = view.findViewById(R.id.registerButton);
        toLogin = view.findViewById(R.id.toLogin);

        auth = FirebaseAuth.getInstance();

        registerButton.setEnabled(false);
        registerButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.transparent, null));

        // кастомный чекбокс
        privacyCheckIcon.setOnClickListener(v -> {
            isPrivacyChecked = !isPrivacyChecked;
            privacyCheckIcon.setImageResource(isPrivacyChecked ?
                    R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);
            updateRegisterButtonState();
        });

        TextWatcher watcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateRegisterButtonState();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        nameEditText.addTextChangedListener(watcher);
        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);
        confirmPasswordEditText.addTextChangedListener(watcher);

        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = result.getUser();
                        if (user != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("avatar", "https://static.tildacdn.com/tild6633-6438-4661-a335-616261336133/9-6-e1591815255406.jpg"); // или URL дефолтной аватарки

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(user.getUid())
                                    .setValue(userMap);

                        }


                        Toast.makeText(getContext(), "Успешная регистрация", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        toLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.auth_fragment_container, new LoginFragment())
                        .commit());

        return view;
    }

    private void updateRegisterButtonState() {
        boolean allFieldsFilled = !nameEditText.getText().toString().trim().isEmpty()
                && !emailEditText.getText().toString().trim().isEmpty()
                && !passwordEditText.getText().toString().trim().isEmpty()
                && !confirmPasswordEditText.getText().toString().trim().isEmpty();

        boolean enabled = allFieldsFilled && isPrivacyChecked;

        registerButton.setEnabled(enabled);
        registerButton.setBackgroundTintList(getResources().getColorStateList(
                enabled ? R.color.white : android.R.color.transparent, null));
        registerButton.setTextColor(getResources().getColor(enabled ? R.color.black : R.color.gray));
    }
}

