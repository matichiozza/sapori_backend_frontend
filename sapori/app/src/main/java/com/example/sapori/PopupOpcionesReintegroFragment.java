package com.example.sapori;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PopupOpcionesReintegroFragment extends DialogFragment {

    private Button btnTarjeta, btnSaldoFavor;
    private TextView txtMontoReintegro, txtPorcentajeReintegro;

    public interface OpcionesReintegroListener {
        void onReintegroTarjeta();
        void onSaldoFavor();
        void onCancelar();
    }

    private OpcionesReintegroListener listener;
    private float montoReintegro;
    private int porcentajeReintegro;

    public void setOpcionesReintegroListener(OpcionesReintegroListener listener) {
        this.listener = listener;
    }

    public void setDatosReintegro(float montoReintegro, int porcentajeReintegro) {
        this.montoReintegro = montoReintegro;
        this.porcentajeReintegro = porcentajeReintegro;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opciones_reintegro, container, false);

        btnTarjeta = view.findViewById(R.id.btnReintegroTarjeta);
        btnSaldoFavor = view.findViewById(R.id.btnSaldoFavor);
        txtMontoReintegro = view.findViewById(R.id.txtMontoReintegro);
        txtPorcentajeReintegro = view.findViewById(R.id.txtPorcentajeReintegro);

        // Mostrar información del reintegro
        txtMontoReintegro.setText("$" + String.format("%.2f", montoReintegro));
        txtPorcentajeReintegro.setText(porcentajeReintegro + "% del valor del curso");

        btnTarjeta.setOnClickListener(v -> {
            if (listener != null) listener.onReintegroTarjeta();
            dismiss();
        });

        btnSaldoFavor.setOnClickListener(v -> {
            if (listener != null) listener.onSaldoFavor();
            dismiss();
        });

        return view;
    }
} 