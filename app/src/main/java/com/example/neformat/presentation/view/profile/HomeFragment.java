package com.example.neformat.presentation.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neformat.R;

import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private ImageView imageOfDay;
    private TextView factOfDay;
    private TextView authorOfDay;
    private LinearLayout leftColumn;
    private LinearLayout rightColumn;
    private LinearLayout imageColumns;
    private boolean isLoading = false;
    private int currentPage = 1;

    private int leftCount = 0;
    private int rightCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageOfDay = view.findViewById(R.id.image_of_day);
        factOfDay = view.findViewById(R.id.fact_of_day);
        authorOfDay = view.findViewById(R.id.author_of_day);

        imageColumns = view.findViewById(R.id.image_columns);

        // создаем две вертикальные колонки
        leftColumn = new LinearLayout(getContext());
        rightColumn = new LinearLayout(getContext());

        leftColumn.setOrientation(LinearLayout.VERTICAL);
        rightColumn.setOrientation(LinearLayout.VERTICAL);

        leftColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        rightColumn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        imageColumns.addView(leftColumn);
        imageColumns.addView(rightColumn);

        // Загружаем картинку дня
        loadImageOfDay();

        // Загружаем остальные изображения
        loadImagesFromServer();

        // Слушатель прокрутки
        imageColumns.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isLoading && hasReachedEnd(scrollY)) {
                isLoading = true;
                currentPage++;
                loadImagesFromServer();
            }
        });

        return view;
    }

    private void loadImageOfDay() {
        String url = "http://localhost:8080/api/designs/today";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String imageUrl = response.getString("imageUrl");
                        String fact = response.optString("fact", "Факт дня недоступен");
                        String author = response.optString("author", "Неизвестный автор");

                        Glide.with(this).load(imageUrl).into(imageOfDay);

                        authorOfDay.setText("Автор " + author);
                        factOfDay.setText(fact);

                        imageOfDay.setOnClickListener(v -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("imageUrl", imageUrl);
                            bundle.putString("author", author); // Передаем имя автора
                            Navigation.findNavController(v).navigate(
                                    R.id.action_homeFragment_to_imageDetailFragment, bundle);
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Ошибка при загрузке", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireActivity()).add(jsonObjectRequest);
    }

    private boolean hasReachedEnd(int scrollY) {
        return scrollY == (imageColumns.getHeight() - imageColumns.getChildAt(0).getHeight());
    }

    private void loadImagesFromServer() {
        String url = "http://localhost:8080/api/designs?page=" + currentPage;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject design = response.getJSONObject(i);
                            String imageUrl = design.getString("imageUrl");
                            String author = design.optString("author", "Неизвестный автор"); // Добавим автора

                            // Просто добавляем в колонны, НЕ перезаписываем imageOfDay
                            addImageToColumns(imageUrl, author); // Передаем авторов в addImageToColumns
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Ошибка обработки", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Ошибка при загрузке", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireActivity()).add(jsonArrayRequest);
    }


    private void addImageToColumns(String imageUrl, String author) {
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(16, 16, 16, 16);

        Glide.with(this).load(imageUrl).into(imageView);

        imageView.setOnClickListener(v -> {
            // Передаем и URL изображения, и имя автора
            Bundle bundle = new Bundle();
            bundle.putString("imageUrl", imageUrl);
            bundle.putString("author", author); // Передаем имя автора
            Navigation.findNavController(v).navigate(
                    R.id.action_homeFragment_to_imageDetailFragment, bundle);
        });

        if (leftCount <= rightCount) {
            leftColumn.addView(imageView);
            leftCount++;
        } else {
            rightColumn.addView(imageView);
            rightCount++;
        }

        isLoading = false;
    }

}
