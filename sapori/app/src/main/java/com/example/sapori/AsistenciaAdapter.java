package com.example.sapori;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.sapori.model.Asistencia;

public class AsistenciaAdapter extends RecyclerView.Adapter<AsistenciaAdapter.ViewHolder> {
    private final List<Asistencia> lista;

    public AsistenciaAdapter(List<Asistencia> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia item = lista.get(position);
        holder.txtClase.setText(String.valueOf(item.getClase()));
        holder.txtFecha.setText(item.getFecha().substring(0,10));
        if (item.getAsistencia() == null || item.getAsistencia().isEmpty()) {
            holder.txtEstado.setText("");
        } else if (item.getAsistencia().equalsIgnoreCase("Presente")) {
            holder.txtEstado.setText("PRESENTE");
            holder.txtEstado.setTextColor(Color.parseColor("#2E7D32"));
        } else if (item.getAsistencia().equalsIgnoreCase("Ausente")) {
            holder.txtEstado.setText("AUSENTE");
            holder.txtEstado.setTextColor(Color.parseColor("#CB2434"));
        } else {
            holder.txtEstado.setText(item.getAsistencia());
            holder.txtEstado.setTextColor(Color.parseColor("#444444"));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtClase, txtFecha, txtEstado;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClase = itemView.findViewById(R.id.txtClase);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEstado = itemView.findViewById(R.id.txtEstado);
        }
    }
} 