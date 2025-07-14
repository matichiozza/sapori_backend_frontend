package com.example.sapori;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.sapori.model.ComentarioValoracion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private final List<ComentarioValoracion> comentarios;

    public ComentarioAdapter(List<ComentarioValoracion> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        ComentarioValoracion comentario = comentarios.get(position);

        if (comentario.getUsuario() != null) {
            holder.txtUsuario.setText(comentario.getUsuario().getNombre());

            String urlFoto = comentario.getUsuario().getFotoPerfil();
            if (urlFoto != null && !urlFoto.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(urlFoto)
                        .placeholder(R.drawable.placeholder)
                        .transform(new CircleCrop())
                        .into(holder.imgPerfil);
            } else {
                holder.imgPerfil.setImageResource(R.drawable.placeholder);
            }

        } else {
            holder.txtUsuario.setText("Usuario anónimo");
            holder.imgPerfil.setImageResource(R.drawable.placeholder);
        }

        if (comentario.getPuntaje() != null) {
            holder.ratingBar.setRating(comentario.getPuntaje());
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        holder.txtComentario.setText(comentario.getTextoComentario());

        if (comentario.getFecha() != null && !comentario.getFecha().isEmpty()) {
            try {
                LocalDateTime fecha = LocalDateTime.parse(comentario.getFecha());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                holder.txtFecha.setText(fecha.format(formatter));
            } catch (Exception e) {
                holder.txtFecha.setText("Fecha inválida");
            }
        } else {
            holder.txtFecha.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsuario, txtComentario, txtFecha;
        RatingBar ratingBar;
        ImageView imgPerfil;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUsuario = itemView.findViewById(R.id.txtUsuarioComentario);
            txtComentario = itemView.findViewById(R.id.txtTextoComentario);
            txtFecha = itemView.findViewById(R.id.txtFechaComentario);
            ratingBar = itemView.findViewById(R.id.ratingBarComentario);
            imgPerfil = itemView.findViewById(R.id.imgPerfilUsuario);
        }
    }
}
