package com.example.sapori;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapori.R;

import java.util.List;

public class ImagenesRecetaAdapter extends RecyclerView.Adapter<ImagenesRecetaAdapter.ImagenViewHolder> {

    private List<String> listaImagenes;
    private Context context;

    public ImagenesRecetaAdapter(Context context, List<String> listaImagenes) {
        this.context = context;
        this.listaImagenes = listaImagenes;
    }

    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_imagen_pager, parent, false);
        return new ImagenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position) {
        String urlImagen = listaImagenes.get(position);

        Glide.with(context)
                .load(urlImagen)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imagenView);
    }

    @Override
    public int getItemCount() {
        return listaImagenes.size();
    }

    static class ImagenViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenView;

        public ImagenViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenView = itemView.findViewById(R.id.imgPagerItem);
        }
    }
}
