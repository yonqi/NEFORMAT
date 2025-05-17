package com.example.neformat.presentation.view.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.*;
import org.json.JSONObject;
public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "fe78c0408a647a394434495a82abf60b";

    private EditText nameEditText;
    private Button saveNameButton, uploadAvatarButton;
    private ImageView avatarPreview;
    private ImageButton backButton;

    private FirebaseUser user;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        saveNameButton = view.findViewById(R.id.saveNameButton);
        uploadAvatarButton = view.findViewById(R.id.saveAvatarButton);
        avatarPreview = view.findViewById(R.id.avatarPreview);
        backButton = view.findViewById(R.id.back_button);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // 1. Загружаем из SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        String cachedName = prefs.getString("name", null);
        String cachedAvatar = prefs.getString("avatar", null);

        if (cachedName != null) {
            nameEditText.setText(cachedName);
        }

        if (cachedAvatar != null && !cachedAvatar.isEmpty()) {
            Glide.with(requireContext()).load(cachedAvatar).into(avatarPreview);
        }

        // 2. Загружаем из Firebase и обновляем кэш
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avatar").getValue(String.class);

                    if (name != null) {
                        nameEditText.setText(name);
                        prefs.edit().putString("name", name).apply();
                    }

                    if (avatar != null && !avatar.isEmpty()) {
                        Glide.with(requireContext()).load(avatar).into(avatarPreview);
                        prefs.edit().putString("avatar", avatar).apply();
                    }
                }

                @Override public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            });
        }

        saveNameButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (!name.isEmpty()) {
                userRef.child("name").setValue(name);
                saveToPrefs("name", name);

                // Скрываем клавиатуру
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                }

                Toast.makeText(getContext(), "Имя обновлено", Toast.LENGTH_SHORT).show();
            }
        });


        uploadAvatarButton.setOnClickListener(v -> openImagePicker());

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.popBackStack();
        });


        return view;
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            avatarPreview.setImageURI(imageUri);
            uploadImageToImgbb(imageUri);
        }
    }

    private void uploadImageToImgbb(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        try {
                            JSONObject json = new JSONObject(body);
                            String url = json.getJSONObject("data").getString("url");

                            if (user != null) {
                                userRef.child("avatar").setValue(url);
                                saveToPrefs("avatar", url);

                                requireActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext()).load(url).into(avatarPreview);
                                    Toast.makeText(getContext(), "Аватар обновлён", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private void saveToPrefs(String key, String value) {
        SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }
}
