package com.example.neformat.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neformat.R;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neformat.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class FavoritesFragment extends Fragment {

    private LinearLayout leftColumn;
    private LinearLayout rightColumn;
    private String firebaseUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Инициализация колонок
        leftColumn = view.findViewById(R.id.left_column);
        rightColumn = view.findViewById(R.id.right_column);

        // Получаем ID текущего пользователя
        firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Загружаем избранное
        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        String url = "http://localhost:8080/api/favorites/list-with-author?firebaseUid=" + firebaseUid;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject favorite = response.getJSONObject(i);
                            String imageUrl = favorite.getString("imageUrl");
                            String author = favorite.getString("author");

                            addImageToColumns(imageUrl, author);
                        }
                    } catch (JSONException e) {
                        showError("Ошибка обработки данных");
                    }
                },
                error -> showError("Ошибка загрузки избранного"));

        Volley.newRequestQueue(requireActivity()).add(request);
    }

    private void addImageToColumns(String imageUrl, String author) {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);

        // Загрузка изображения
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView);

        // Обработка клика
        imageView.setOnClickListener(v -> navigateToDetail(imageUrl, author));

        // Добавление в колонку с меньшим количеством элементов
        if (leftColumn.getChildCount() <= rightColumn.getChildCount()) {
            leftColumn.addView(imageView);
        } else {
            rightColumn.addView(imageView);
        }
    }

    private void navigateToDetail(String imageUrl, String author) {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", imageUrl);
        bundle.putString("author", author);
        Navigation.findNavController(requireView()).navigate(
                R.id.action_favoritesFragment_to_imageDetailFragment, bundle);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}