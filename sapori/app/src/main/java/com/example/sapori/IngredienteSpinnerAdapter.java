package com.example.sapori;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteReceta;

import java.util.List;

public class IngredienteSpinnerAdapter extends ArrayAdapter<IngredienteReceta> {

    private Context context;
    private List<IngredienteReceta> ingredienteRecetas;
    private int selectedPosition = -1;

    public IngredienteSpinnerAdapter(@NonNull Context context, @NonNull List<IngredienteReceta> ingredienteRecetas) {
        super(context, R.layout.spinner_ingrediente_item, ingredienteRecetas);
        this.context = context;
        this.ingredienteRecetas = ingredienteRecetas;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ingredienteRecetas.size();
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_ingrediente_selected_item, parent, false);
        }

        TextView nombre = convertView.findViewById(R.id.txtIngredienteNombre);
        TextView cantidad = convertView.findViewById(R.id.txtIngredienteCant);
        ImageView imagen = convertView.findViewById(R.id.imgIngrediente);

        IngredienteReceta ingReceta = ingredienteRecetas.get(position);

        if (position == 0 || ingReceta == null) {
            // Primer elemento o null: mensaje por defecto, sin botones ni imagen
            nombre.setText("Seleccioná un ingrediente");
            cantidad.setText("");
            imagen.setVisibility(View.GONE);
        } else {
            Ingrediente ing = ingReceta.getIngrediente();

            nombre.setText(ing.getNombre());
            cantidad.setText(formatearCantidad(ingReceta));
            imagen.setVisibility(View.VISIBLE);
            Glide.with(context).load(ing.getImagenUrl()).into(imagen);


        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_ingrediente_dropdown_item, parent, false);
        }

        TextView nombre = convertView.findViewById(R.id.txtIngredienteNombre);
        TextView cantidad = convertView.findViewById(R.id.txtIngredienteCant);
        ImageView imagen = convertView.findViewById(R.id.imgIngrediente);
        View separador = convertView.findViewById(R.id.separador);

        if (position == 0) {
            nombre.setText("Seleccioná un ingrediente");
            cantidad.setText("");
            imagen.setVisibility(View.GONE);
            separador.setVisibility(View.GONE);
        } else {
            IngredienteReceta ingredienteReceta = ingredienteRecetas.get(position);
            Ingrediente ingrediente = ingredienteReceta.getIngrediente();

            nombre.setText(ingrediente.getNombre());
            cantidad.setText(formatearCantidad(ingredienteReceta));
            imagen.setVisibility(View.VISIBLE);
            Glide.with(context).load(ingrediente.getImagenUrl()).into(imagen);

            separador.setVisibility(position == ingredienteRecetas.size() - 1 ? View.GONE : View.VISIBLE);
        }

        if (position == selectedPosition) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.negro_claro));
            nombre.setTextColor(ContextCompat.getColor(context, R.color.white));
            cantidad.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.blanco_claro));
            nombre.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            cantidad.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        }

        return convertView;
    }

    private String formatearCantidad(IngredienteReceta ing) {
        String unidad = ing.getUnidad();
        if (unidad != null) {
            if (unidad.equalsIgnoreCase("unidad") && ing.getCantidad() == 1) {
                return "1 unidad";
            } else if (unidad.equalsIgnoreCase("unidad")) {
                return (int) ing.getCantidad() + " unidades";
            } else {
                return ((int) ing.getCantidad()) + " " + unidad;
            }
        } else {
            // Si unidad es null, devolver solo la cantidad como string
            return String.valueOf((int) ing.getCantidad());
        }
    }
}
