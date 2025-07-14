package com.example.sapori;

import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.List;

// IngredienteAdapter3.java
public class IngredienteAdapter3 extends RecyclerView.Adapter<IngredienteAdapter3.IngredienteViewHolder> {

    private List<IngredienteReceta> ingredientesOriginales;  // datos base (sin modificar)
    private List<IngredienteReceta> ingredientesVisuales;    // datos que se ven en pantalla

    public IngredienteAdapter3(List<IngredienteReceta> ingredientes) {
        this.ingredientesOriginales = new ArrayList<>();
        this.ingredientesVisuales = new ArrayList<>();

        for (IngredienteReceta ir : ingredientes) {
            this.ingredientesOriginales.add(clonarIngredienteReceta(ir));
            this.ingredientesVisuales.add(clonarIngredienteReceta(ir));
        }
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente_2, parent, false);
        return new IngredienteViewHolder(v);
    }

    public List<IngredienteReceta> getIngredientesEscalados() {
        return ingredientesVisuales;
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        if (ingredientesVisuales == null || position < 0 || position >= ingredientesVisuales.size()) return;

        IngredienteReceta ingredienteReceta = ingredientesVisuales.get(position);
        if (ingredienteReceta == null || ingredienteReceta.getIngrediente() == null) {
            holder.txtNombre.setText("Ingrediente desconocido");
            holder.txtCantidad.setText("");
            holder.imgIngrediente.setImageResource(R.drawable.placeholder);
            return;
        }

        String nombre = ingredienteReceta.getIngrediente().getNombre();
        String cantidad = String.format("%.1f %s", ingredienteReceta.getCantidad(), ingredienteReceta.getUnidad());

        holder.txtNombre.setText(nombre);
        holder.txtNombre.setTextColor(0xFF363636);

        holder.txtCantidad.setText(cantidad);
        holder.txtCantidad.setTextColor(0xFF969696);

        String urlImagen = ingredienteReceta.getIngrediente().getImagenUrl();
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
        return ingredientesVisuales != null ? ingredientesVisuales.size() : 0;
    }

    public void actualizarCantidadesConFactor(float factor) {
        for (int i = 0; i < ingredientesOriginales.size(); i++) {
            IngredienteReceta original = ingredientesOriginales.get(i);
            IngredienteReceta visual = ingredientesVisuales.get(i);
            visual.setCantidad(original.getCantidad() * factor);
        }
        notifyDataSetChanged();
    }

    private IngredienteReceta clonarIngredienteReceta(IngredienteReceta original) {
        IngredienteReceta clon = new IngredienteReceta();
        clon.setId(original.getId());
        clon.setIngrediente(original.getIngrediente()); // se puede compartir
        clon.setCantidad(original.getCantidad());
        clon.setUnidad(original.getUnidad());
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
