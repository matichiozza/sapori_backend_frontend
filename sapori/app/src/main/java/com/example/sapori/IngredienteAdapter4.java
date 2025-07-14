package com.example.sapori;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sapori.model.IngredienteDTO;

import java.util.ArrayList;
import java.util.List;

public class IngredienteAdapter4 extends RecyclerView.Adapter<IngredienteAdapter4.IngredienteViewHolder> {

    private static final String TAG = "IngredienteAdapter4";

    private List<IngredienteDTO> ingredientesOriginales;
    private List<IngredienteDTO> ingredientesVisuales;

    public IngredienteAdapter4(List<IngredienteDTO> ingredientes) {
        this.ingredientesOriginales = new ArrayList<>();
        this.ingredientesVisuales = new ArrayList<>();

        if (ingredientes != null) {
            for (IngredienteDTO dto : ingredientes) {
                this.ingredientesOriginales.add(clonarIngrediente(dto));
                this.ingredientesVisuales.add(clonarIngrediente(dto));
            }
            Log.d(TAG, "Adapter creado con " + ingredientes.size() + " ingredientes");
        } else {
            Log.d(TAG, "Adapter creado con lista null o vacía");
        }
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_2, parent, false);
        return new IngredienteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        if (ingredientesVisuales == null || position < 0 || position >= ingredientesVisuales.size()) {
            Log.w(TAG, "Posición inválida o lista null en onBindViewHolder: " + position);
            return;
        }

        IngredienteDTO ingredienteDTO = ingredientesVisuales.get(position);

        if (ingredienteDTO == null) {
            Log.w(TAG, "Ingrediente en posición " + position + " es null");
            holder.txtNombre.setText("Ingrediente desconocido");
            holder.txtCantidad.setText("");
            holder.imgIngrediente.setImageResource(R.drawable.placeholder);
            return;
        }

        Log.d(TAG, "Mostrando ingrediente posición " + position + ": " +
                "Nombre='" + ingredienteDTO.getNombre() + "', " +
                "Cantidad='" + ingredienteDTO.getCantidad() + "', " +
                "CantidadOriginal='" + ingredienteDTO.getCantidadOriginal() + "'");

        holder.txtNombre.setText(ingredienteDTO.getNombre());
        holder.txtNombre.setTextColor(0xFF363636);

        String cantidad = ingredienteDTO.getCantidad();
        String original = ingredienteDTO.getCantidadOriginal();

        if (cantidad == null || cantidad.trim().isEmpty()) {
            holder.txtCantidad.setText("");
        } else if (original != null && !original.trim().isEmpty()) {
            holder.txtCantidad.setText(original + " → " + cantidad);
            holder.txtCantidad.setTextColor(0xFF969696);
        } else {
            holder.txtCantidad.setText(cantidad);
            holder.txtCantidad.setTextColor(0xFF969696);
        }

        String urlImagen = ingredienteDTO.getImagenUrl();
        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(urlImagen)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imgIngrediente);
        } else {
            holder.imgIngrediente.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        int count = ingredientesVisuales != null ? ingredientesVisuales.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public List<IngredienteDTO> getIngredientesEscalados() {
        return ingredientesVisuales;
    }

    public void actualizarCantidadesConFactor(float factor) {
        Log.d(TAG, "actualizarCantidadesConFactor con factor: " + factor);
        for (int i = 0; i < ingredientesOriginales.size(); i++) {
            IngredienteDTO original = ingredientesOriginales.get(i);
            IngredienteDTO visual = ingredientesVisuales.get(i);

            float cantidadOriginal = 0;
            try {
                if (original.getCantidad() != null && !original.getCantidad().isEmpty()) {
                    cantidadOriginal = Float.parseFloat(original.getCantidad());
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error parsing cantidadOriginal para ingrediente " + original.getNombre(), e);
                cantidadOriginal = 0;
            }

            float cantidadEscalada = cantidadOriginal * factor;
            String unidad = original.getUnidad() != null ? original.getUnidad() : "";

            visual.setCantidad(String.format("%.1f %s", cantidadEscalada, unidad).trim());
            visual.setCantidadOriginal(String.format("%.1f %s", cantidadOriginal, unidad).trim());
        }
        notifyDataSetChanged();
    }

    private IngredienteDTO clonarIngrediente(IngredienteDTO original) {
        IngredienteDTO clon = new IngredienteDTO(
                original.getNombre(),
                original.getCantidad(),
                original.getUnidad(),
                original.getImagenUrl()
        );
        clon.setCantidadOriginal(original.getCantidadOriginal());
        return clon;
    }

    static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtCantidad;
        ImageView imgIngrediente;

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreIngrediente);
            txtCantidad = itemView.findViewById(R.id.txtCantidadIngrediente);
            imgIngrediente = itemView.findViewById(R.id.imagen_ingrediente);
        }
    }
}