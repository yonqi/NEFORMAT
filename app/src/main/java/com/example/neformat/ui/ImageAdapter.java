package com.example.neformat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neformat.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Fragment fragment;
    private final List<String> imageUrls;
    private final List<String> authors;  // Список авторов

    public ImageAdapter(Fragment fragment, List<String> imageUrls, List<String> authors) {
        this.fragment = fragment;
        this.imageUrls = imageUrls;
        this.authors = authors;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.item_image_view);
        }
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        String authorName = authors.get(position); // Получаем имя автора из списка

        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .into(holder.imageView);

        // Клик по изображению -> переход в новый фрагмент
        holder.imageView.setOnClickListener(v -> {
            ImageDetailFragment fragmentDetail = ImageDetailFragment.newInstance(imageUrl, authorName); // Передаем оба параметра
            fragment.getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });


    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
}
