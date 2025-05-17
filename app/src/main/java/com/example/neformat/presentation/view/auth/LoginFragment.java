package com.example.neformat.presentation.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.neformat.MainActivity;
import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView toRegister;

    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        toRegister = view.findViewById(R.id.toRegister);

        auth = FirebaseAuth.getInstance();

        // Изначально кнопка неактивна
        loginButton.setEnabled(false);

        // Слушатель ввода
        TextWatcher inputWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                boolean enable = !email.isEmpty() && !password.isEmpty();

                loginButton.setEnabled(enable);
                loginButton.setBackgroundTintList(getResources().getColorStateList(
                        enable ? R.color.white : android.R.color.transparent, null));
                loginButton.setTextColor(getResources().getColor(enable ? R.color.black : R.color.gray));

            }
        };

        emailEditText.addTextChangedListener(inputWatcher);
        passwordEditText.addTextChangedListener(inputWatcher);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(getContext(), "Успешный вход", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        requireActivity().finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        TextView forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.auth_fragment_container, new ForgotPasswordFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        toRegister.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.auth_fragment_container, new RegisterFragment())
                        .commit());

        return view;
    }
}
