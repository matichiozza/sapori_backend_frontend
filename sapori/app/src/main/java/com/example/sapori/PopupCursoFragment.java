package com.example.sapori;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PopupCursoFragment extends DialogFragment {

    private Button btnBaja, btnOk;

    public interface PopupListener {
        void onConfirm();
        void onCancel();
    }

    private PopupListener listener;

    public void setPopupListener(PopupListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 85% ancho pantalla
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
        View view = inflater.inflate(R.layout.fragment_darme_baja, container, false);

        btnBaja = view.findViewById(R.id.popup_button_baja);
        btnOk = view.findViewById(R.id.popup_button_ok);

        btnBaja.setOnClickListener(v -> {
            if (listener != null) {
                // Cerrar este popup y mostrar el popup de opciones de reintegro
                dismiss();
                listener.onConfirm();
            }
        });

        btnOk.setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
            dismiss();
        });

        return view;
    }
}