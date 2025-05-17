package com.example.neformat.presentation.view.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = view.findViewById(R.id.emailEditText);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);
        ImageButton backButton = view.findViewById(R.id.back_button);

        // Слушатель текста
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enabled = !s.toString().trim().isEmpty();
                resetPasswordButton.setEnabled(enabled);
                resetPasswordButton.setBackgroundTintList(getResources().getColorStateList(
                        enabled ? R.color.white : android.R.color.transparent, null));
                resetPasswordButton.setTextColor(getResources().getColor(
                        enabled ? R.color.black : R.color.gray, null));
            }
        });

        // Обработка нажатия кнопки
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Введите email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Письмо отправлено на " + email, Toast.LENGTH_LONG).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "Ошибка";
                            Toast.makeText(getContext(), "Ошибка: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Назад
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
