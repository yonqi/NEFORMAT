package com.example.neformat.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.neformat.ApiClient;
import com.example.neformat.ApiService;
import com.example.neformat.R;
import com.example.neformat.Design;
import com.example.neformat.FavoriteRequest;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDetailFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_AUTHOR_NAME = "author";

    private Long designId = null;
    private boolean isFavorite = false;
    private ApiService apiService;
    private String firebaseUid;
    private String imageUrl;

    public static ImageDetailFragment newInstance(String imageUrl, String authorName) {
        ImageDetailFragment fragment = new ImageDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_AUTHOR_NAME, authorName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_detail, container, false);

        firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        apiService = ApiClient.getInstance().create(ApiService.class);

        imageUrl = getArguments() != null ? getArguments().getString(ARG_IMAGE_URL) : null;
        String authorName = getArguments() != null ? getArguments().getString(ARG_AUTHOR_NAME) : "Неизвестен";

        ImageView imageView = view.findViewById(R.id.full_image_view);
        ImageButton backButton = view.findViewById(R.id.back_button);
        ImageButton favoriteButton = view.findViewById(R.id.favorite_button);
        TextView authorTextView = view.findViewById(R.id.author_name);

        authorTextView.setText("Автор " + authorName);

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(imageView);
            fetchDesignId(imageUrl);
        }

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        favoriteButton.setOnClickListener(v -> {
            if (designId == null) {
                Toast.makeText(getContext(), "Дизайн ещё не загружен", Toast.LENGTH_SHORT).show();
                return;
            }

            isFavorite = !isFavorite;

            FavoriteRequest request = new FavoriteRequest(firebaseUid, designId);

            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
                apiService.addToFavorites(request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(getContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star);
                apiService.removeFromFavorites(request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    private void fetchDesignId(String imageUrl) {
        apiService.getDesignByUrl(imageUrl).enqueue(new Callback<Design>() {
            @Override

            public void onResponse(Call<Design> call, Response<Design> response) {
                Log.d("GET_DESIGN_BY_URL", "Запрос по imageUrl = " + imageUrl);

                Log.d("SERVER_RESPONSE", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    designId = response.body().getId();
                    Log.d("SERVER_RESPONSE", "Design ID: " + designId);
                } else {
                    Log.e("SERVER_RESPONSE", "Design not found: " + response.message());
                    Toast.makeText(getContext(), "Дизайн не найден", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Design> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
