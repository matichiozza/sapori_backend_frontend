package com.example.sapori;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SugerenciasAdapter extends RecyclerView.Adapter<SugerenciasAdapter.SugerenciaViewHolder> {

    private List<String> sugerencias = new ArrayList<>();
    private OnItemClickListener listener;

    // Interfaz para click
    public interface OnItemClickListener {
        void onItemClick(String sugerencia);
    }

    // Setter para el listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSugerencias(List<String> nuevasSugerencias) {
        if (nuevasSugerencias == null) {
            this.sugerencias.clear();
        } else {
            this.sugerencias = nuevasSugerencias;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SugerenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sugerencia, parent, false);
        return new SugerenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SugerenciaViewHolder holder, int position) {
        String sugerencia = sugerencias.get(position);
        holder.tvSugerencia.setText(sugerencia);

        // Setear click listener en el itemView
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(sugerencia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sugerencias.size();
    }

    static class SugerenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvSugerencia;

        public SugerenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSugerencia = itemView.findViewById(R.id.tvSugerencia);
        }
    }
}