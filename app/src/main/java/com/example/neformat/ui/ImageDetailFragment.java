package com.example.neformat.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.example.neformat.FavoriteRequest;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDetailFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_AUTHOR_NAME = "author";

    private boolean isFavorite = false;
    private ApiService apiService;
    private String firebaseUid;
    private String imageUrl;
    private ImageButton favoriteButton;

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
        favoriteButton = view.findViewById(R.id.favorite_button);
        TextView authorTextView = view.findViewById(R.id.author_name);

        authorTextView.setText("Автор " + authorName);

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(imageView);
            checkIfFavorite(); // Проверяем при загрузке
        }

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        favoriteButton = view.findViewById(R.id.favorite_button);
        TextView favoriteText = view.findViewById(R.id.favorite_text);

        favoriteButton.setOnClickListener(v -> {
            // Вибрация
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }

            // Анимация вращения и обновление
            favoriteButton.animate()
                    .rotationBy(isFavorite ? -360f : 360f)
                    .setDuration(500)
                    .withEndAction(this::toggleFavorite)
                    .start();

            // Показ текста
            int colorRes = isFavorite ? R.color.gray : R.color.red;
            favoriteText.setTextColor(getResources().getColor(colorRes, null));

            String message = isFavorite ?  "Удалено\nиз избранного" : "Добавлено\nв избранное";
            favoriteText.setText(message);
            favoriteText.setVisibility(View.VISIBLE);
            favoriteText.setAlpha(0f);
            favoriteText.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction(() -> favoriteText.postDelayed(() -> {
                        favoriteText.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> favoriteText.setVisibility(View.GONE))
                                .start();
                    }, 1500))
                    .start();

        });

        checkIfFavorite();
        return view;
    }

    private void checkIfFavorite() {
        apiService.checkFavorite(firebaseUid, imageUrl).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body();
                    updateFavoriteButton();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("ImageDetail", "Ошибка при проверке избранного", t);
            }
        });
    }

    private void toggleFavorite() {
        if (imageUrl == null) {
            Toast.makeText(getContext(), "Изображение не загружено", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavorite = !isFavorite;
        updateFavoriteButton();

        FavoriteRequest request = new FavoriteRequest(firebaseUid, imageUrl);

        if (isFavorite) {
            apiService.addToFavorites(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Успешно добавлено
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    revertFavoriteState();
                }
            });
        } else {
            apiService.removeFromFavorites(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Успешно удалено
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    revertFavoriteState();
                }
            });
        }
    }

    private void updateFavoriteButton() {
        favoriteButton.setImageResource(
                isFavorite ? R.drawable.star : R.drawable.ic_star
        );
    }

    private void revertFavoriteState() {
        isFavorite = !isFavorite;
        updateFavoriteButton();
    }
}