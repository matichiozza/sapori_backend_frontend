package com.example.sapori;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapori.R;

import java.util.List;

public class ImagenURLAdapter extends RecyclerView.Adapter<ImagenURLAdapter.ImagenViewHolder> {

    private final List<String> imagenes;

    public ImagenURLAdapter(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_imagen_url, parent, false);
        return new ImagenViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position) {
        String url = imagenes.get(position);
        Glide.with(holder.imagen.getContext())
                .load(url)
                .centerCrop()
                .into(holder.imagen);
    }

    @Override
    public int getItemCount() {
        return imagenes != null ? imagenes.size() : 0;
    }

    static class ImagenViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;

        public ImagenViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen_url);
        }
    }
}
