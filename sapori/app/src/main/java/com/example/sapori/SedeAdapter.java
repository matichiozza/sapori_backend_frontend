package com.example.sapori;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.CursoSede;

import java.util.List;

public class SedeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CursoSede> sedes;
    private int selectedPosition = -1;
    private OnSedeSelectedListener sedeSelectedListener;
    private OnContinuarClickListener continuarClickListener;
    private OnVerDetalleClickListener verDetalleClickListener;

    private static final int VIEW_TYPE_SEDE = 0;
    private static final int VIEW_TYPE_BOTON = 1;

    public interface OnSedeSelectedListener {
        void onSedeSelected(CursoSede sede, int position);
    }

    public interface OnContinuarClickListener {
        void onContinuarClick();
    }

    public interface OnVerDetalleClickListener {
        void onVerDetalleClick(CursoSede sede);
    }

    public SedeAdapter(List<CursoSede> sedes) {
        this.sedes = sedes;
    }

    public void setOnSedeSelectedListener(OnSedeSelectedListener listener) {
        this.sedeSelectedListener = listener;
    }

    public void setOnContinuarClickListener(OnContinuarClickListener listener) {
        this.continuarClickListener = listener;
    }

    public void setOnVerDetalleClickListener(OnVerDetalleClickListener listener) {
        this.verDetalleClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == sedes.size()) {
            return VIEW_TYPE_BOTON;
        }
        return VIEW_TYPE_SEDE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEDE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sede, parent, false);
            return new SedeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_boton_continuar, parent, false);
            return new BotonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SedeViewHolder) {
            SedeViewHolder sedeHolder = (SedeViewHolder) holder;
            CursoSede sede = sedes.get(position);
            sedeHolder.txtNombreSede.setText(sede.getSede().getNombre());
            
            // Descuento
            if (sede.isHayDescuento() && sede.getDescuento() > 0) {
                sedeHolder.txtDescuento.setText(sede.getDescuento() + "% OFF");
                sedeHolder.txtDescuento.setVisibility(View.VISIBLE);
                sedeHolder.itemView.setBackground(ContextCompat.getDrawable(sedeHolder.itemView.getContext(), R.drawable.bg_card_sede_selector_descuento));
            } else {
                sedeHolder.txtDescuento.setVisibility(View.GONE);
                sedeHolder.itemView.setBackground(ContextCompat.getDrawable(sedeHolder.itemView.getContext(), R.drawable.bg_card_sede_selector));
            }
            
            // RadioButton
            sedeHolder.radioButton.setChecked(position == selectedPosition);
            
            // Click listeners sin animaciones
            sedeHolder.radioButton.setOnClickListener(v -> {
                int prevSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(prevSelected);
                notifyItemChanged(selectedPosition);
                if (sedeSelectedListener != null) {
                    sedeSelectedListener.onSedeSelected(sede, position);
                }
            });
            
            // Click listener para todo el item (excepto el botón Ver detalle)
            sedeHolder.itemView.setOnClickListener(v -> {
                int prevSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(prevSelected);
                notifyItemChanged(selectedPosition);
                if (sedeSelectedListener != null) {
                    sedeSelectedListener.onSedeSelected(sede, position);
                }
            });
            
            // Botón Ver detalle
            sedeHolder.btnVerDetalle.setOnClickListener(v -> {
                if (verDetalleClickListener != null) {
                    verDetalleClickListener.onVerDetalleClick(sede);
                }
            });
            
        } else if (holder instanceof BotonViewHolder) {
            BotonViewHolder botonHolder = (BotonViewHolder) holder;
            botonHolder.btnContinuar.setOnClickListener(v -> {
                if (continuarClickListener != null) {
                    continuarClickListener.onContinuarClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sedes.size() + 1; // +1 para el botón
    }

    public void actualizarSedes(List<CursoSede> nuevasSedes) {
        this.sedes = nuevasSedes;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public CursoSede getSedeSeleccionada() {
        if (selectedPosition >= 0 && selectedPosition < sedes.size()) {
            return sedes.get(selectedPosition);
        }
        return null;
    }

    static class SedeViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView txtNombreSede, txtDescuento, btnVerDetalle;
        
        public SedeViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            txtNombreSede = itemView.findViewById(R.id.txtNombreSede);
            txtDescuento = itemView.findViewById(R.id.txtDescuento);
            btnVerDetalle = itemView.findViewById(R.id.btnVerDetalle);
        }
    }

    static class BotonViewHolder extends RecyclerView.ViewHolder {
        TextView btnContinuar;
        
        public BotonViewHolder(@NonNull View itemView) {
            super(itemView);
            btnContinuar = itemView.findViewById(R.id.btnContinuarPago);
        }
    }
} 