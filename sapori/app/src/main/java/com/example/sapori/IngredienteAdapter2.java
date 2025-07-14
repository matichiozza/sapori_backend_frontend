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
import com.example.sapori.model.IngredienteReceta;
import com.google.gson.Gson;

import java.util.List;

public class IngredienteAdapter2 extends RecyclerView.Adapter<IngredienteAdapter2.IngredienteViewHolder> {

    private static final String TAG = "IngredienteAdapter";

    private final List<IngredienteReceta> ingredientes;

    public IngredienteAdapter2(List<IngredienteReceta> ingredientes) {
        this.ingredientes = ingredientes;
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

        if (ingredientes == null) {
            return;
        }

        if (position < 0 || position >= ingredientes.size()) {
            return;
        }

        IngredienteReceta ingredienteReceta = ingredientes.get(position);
        if (ingredienteReceta == null) {
            return;
        }

        if (ingredienteReceta.getIngrediente() != null) {
            String nombreIngrediente = ingredienteReceta.getIngrediente().getNombre();
            holder.txtNombre.setText(nombreIngrediente);
            holder.txtNombre.setTextColor(0xFF363636);

            String cantidadUnidad = ingredienteReceta.getCantidad() + " " + ingredienteReceta.getUnidad();
            holder.txtCantidad.setText(cantidadUnidad);
            holder.txtCantidad.setTextColor(0xFF969696);

            String urlImagen = ingredienteReceta.getIngrediente().getImagenUrl();

            if (urlImagen != null && !urlImagen.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(urlImagen)
                        .placeholder(R.drawable.placeholder)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                           DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.imgIngrediente);
            } else {
                holder.imgIngrediente.setImageResource(R.drawable.placeholder);
            }
        } else {
            holder.txtNombre.setText("Ingrediente desconocido");
            holder.txtCantidad.setText("");
            holder.imgIngrediente.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        int count = ingredientes != null ? ingredientes.size() : 0;
        return count;
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
