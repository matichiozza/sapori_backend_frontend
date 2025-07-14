package com.example.sapori;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.R;

import java.util.List;

public class IngredienteStringAdapter extends RecyclerView.Adapter<IngredienteStringAdapter.ViewHolder> {

    private final List<String> ingredientes;

    public IngredienteStringAdapter(List<String> ingredientes) {
        this.ingredientes = ingredientes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String nombre = ingredientes.get(position);
        holder.nombreIngrediente.setText(nombre);
        holder.imagenIngrediente.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreIngrediente;
        ImageView imagenIngrediente;

        ViewHolder(View itemView) {
            super(itemView);
            nombreIngrediente = itemView.findViewById(R.id.nombre_ingrediente);
            imagenIngrediente = itemView.findViewById(R.id.imagen_ingrediente);
        }
    }
}
