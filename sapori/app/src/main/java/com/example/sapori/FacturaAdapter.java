package com.example.sapori;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Pago;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacturaAdapter extends RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder> {

    private List<Pago> facturas;

    public FacturaAdapter(List<Pago> facturas) {
        this.facturas = facturas;
    }

    @NonNull
    @Override
    public FacturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_factura, parent, false);
        return new FacturaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacturaViewHolder holder, int position) {
        Pago factura = facturas.get(position);
        holder.bind(factura);
    }

    @Override
    public int getItemCount() {
        return facturas.size();
    }

    public void actualizarFacturas(List<Pago> nuevasFacturas) {
        this.facturas = nuevasFacturas;
        notifyDataSetChanged();
    }

    class FacturaViewHolder extends RecyclerView.ViewHolder {
        private TextView txtCurso;
        private TextView txtImporte;
        private TextView txtFechaVencimiento;
        private TextView txtEstado;

        public FacturaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCurso = itemView.findViewById(R.id.txtCurso);
            txtImporte = itemView.findViewById(R.id.txtImporte);
            txtFechaVencimiento = itemView.findViewById(R.id.txtFechaVencimiento);
            txtEstado = itemView.findViewById(R.id.txtEstado);
        }

        public void bind(Pago factura) {
            if (factura.getCurso() != null) {
                String cursoText = factura.getCurso().getNombre();
                if (factura.getSede() != null) {
                    cursoText += " - " + factura.getSede().getNombre();
                }
                txtCurso.setText(cursoText);
            } else {
                txtCurso.setText("Sin curso asociado");
            }

            txtImporte.setText("$" + String.format("%.2f", factura.getImporte()));

            if (factura.getFechaInicioCurso() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String fechaFormateada = factura.getFechaInicioCurso().format(formatter);
                txtFechaVencimiento.setText("Inicia: " + fechaFormateada);
            } else {
                txtFechaVencimiento.setText("Sin fecha de inicio");
            }

            // Configurar color según estado
            switch (factura.getEstado()) {
                case A_PAGAR:
                    txtEstado.setText("A pagar");
                    txtEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    break;
                case PAGO:
                    txtEstado.setText("Pagada");
                    txtEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case REEMBOLSADO:
                    txtEstado.setText("Reembolso");
                    txtEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    break;
            }
        }
    }
} 