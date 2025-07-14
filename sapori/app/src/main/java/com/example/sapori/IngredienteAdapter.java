package com.example.sapori;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Ingrediente;

import java.util.List;

public class IngredienteAdapter extends ListAdapter<Ingrediente, IngredienteAdapter.IngredienteViewHolder> {

    protected IngredienteAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Ingrediente> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Ingrediente>() {
                @Override
                public boolean areItemsTheSame(@NonNull Ingrediente oldItem, @NonNull Ingrediente newItem) {
                    // Cambia según tu modelo. Si tienes un id único, mejor usarlo.
                    return oldItem.getNombre().equals(newItem.getNombre());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Ingrediente oldItem, @NonNull Ingrediente newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingrediente, parent, false);
        return new IngredienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imagen;
        private final TextView nombre;

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen_ingrediente);
            nombre = itemView.findViewById(R.id.nombre_ingrediente);
        }

        void bind(Ingrediente ingrediente) {
            nombre.setText(ingrediente.getNombre());
            Glide.with(imagen.getContext())
                    .load(ingrediente.getImagenUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(imagen);
        }
    }
}