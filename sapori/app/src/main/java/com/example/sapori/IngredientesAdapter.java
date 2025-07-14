package com.example.sapori;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteDTO;

import java.util.List;

public class IngredientesAdapter extends RecyclerView.Adapter<IngredientesAdapter.ViewHolder> {

    private List<IngredienteDTO> ingredientes;

    public IngredientesAdapter(List<IngredienteDTO> ingredientes) {
        this.ingredientes = ingredientes;
    }

    @NonNull
    @Override
    public IngredientesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_2, parent, false);  // <-- tu layout con el recuadro
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientesAdapter.ViewHolder holder, int position) {
        IngredienteDTO ingrediente = ingredientes.get(position);
        holder.txtNombre.setText(ingrediente.getNombre());

        double cantidadDouble;
        try {
            cantidadDouble = Double.parseDouble(ingrediente.getCantidad());
        } catch (NumberFormatException e) {
            cantidadDouble = 0.0;
        }

        String cantidadFormateada = String.format(java.util.Locale.getDefault(), "%.1f", cantidadDouble);
        holder.txtCantidad.setText(cantidadFormateada);

        holder.txtUnidad.setText(String.valueOf(ingrediente.getUnidad()));

        Glide.with(holder.imagenIngrediente.getContext())
                .load(ingrediente.getImagenUrl())
                .centerInside()
                .into(holder.imagenIngrediente);
    }

    @Override
    public int getItemCount() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenIngrediente;
        TextView txtNombre;
        TextView txtCantidad;
        TextView txtUnidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenIngrediente = itemView.findViewById(R.id.imagen_ingrediente);
            txtNombre = itemView.findViewById(R.id.txtNombreIngrediente);
            txtCantidad = itemView.findViewById(R.id.txtCantidadIngrediente);
            txtUnidad = itemView.findViewById(R.id.txtunidadIngrediente);
        }
    }
}