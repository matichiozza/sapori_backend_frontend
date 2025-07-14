package com.example.sapori.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.R;
import com.example.sapori.model.PasoReceta;

import java.util.List;

public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.PasoViewHolder> {

    private final List<PasoReceta> pasos;

    public PasoAdapter(List<PasoReceta> pasos) {
        this.pasos = pasos;
    }

    @NonNull
    @Override
    public PasoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso, parent, false);
        return new PasoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PasoViewHolder holder, int position) {
        PasoReceta paso = pasos.get(position);
        holder.txtNumero.setText("Paso " + paso.getNumeroPaso());
        holder.txtDescripcion.setText(paso.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return pasos != null ? pasos.size() : 0;
    }

    static class PasoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtDescripcion;

        public PasoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumeroPaso);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionPaso);
        }
    }
}
