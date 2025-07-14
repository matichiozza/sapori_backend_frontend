package com.example.sapori;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteDTO;
import com.example.sapori.model.IngredienteReceta;

public class PopupIngrediente extends DialogFragment {

    private IngredienteReceta receta;
    private OnIngredienteUpdatedListener listener;

    public interface OnIngredienteUpdatedListener {
        void mostrarIngredientesSeleccionados();
    }

    public static PopupIngrediente newInstance(IngredienteReceta receta) {
        PopupIngrediente fragment = new PopupIngrediente();
        Bundle args = new Bundle();
        args.putSerializable("receta", receta);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Intentar asignar listener desde el contexto o fragmento padre
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnIngredienteUpdatedListener) {
            listener = (OnIngredienteUpdatedListener) parentFragment;
        } else if (context instanceof OnIngredienteUpdatedListener) {
            listener = (OnIngredienteUpdatedListener) context;
        } else {
            listener = null; // no asignado, cuidado al usarlo
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receta = (IngredienteReceta) getArguments().getSerializable("receta");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popupingrediente, container, false);

        Ingrediente ingrediente = receta.getIngrediente();

        TextView title = view.findViewById(R.id.popup_title);
        title.setText(ingrediente.getNombre());

        EditText editCantidad = view.findViewById(R.id.popup_edittext);
        Spinner spinnerUnidad = view.findViewById(R.id.popup_spinner_unidad);

        String[] unidades = new String[]{
                "g", "kg", "mg", "l", "ml", "cdta", "cda", "taza",
                "u", "pizca", "paquete", "sobre", "lb", "oz"
        };

        ArrayAdapter<String> adapterUnidad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, unidades);
        adapterUnidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidad.setAdapter(adapterUnidad);

        if (receta.getCantidad() > 0) {
            editCantidad.setText(String.valueOf(receta.getCantidad()));
        } else {
            editCantidad.setText("");
        }

        if (receta.getUnidad() != null && !receta.getUnidad().isEmpty()) {
            int position = adapterUnidad.getPosition(receta.getUnidad());
            if (position >= 0) {
                spinnerUnidad.setSelection(position);
            }
        }

        Button btnOk = view.findViewById(R.id.popup_button_ok);
        btnOk.setOnClickListener(v -> {
            String cantidadStr = editCantidad.getText().toString().trim();
            String unidad = spinnerUnidad.getSelectedItem().toString();

            if (cantidadStr.isEmpty()) {
                Toast.makeText(requireContext(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double cantidad = Double.parseDouble(cantidadStr);
                if (cantidad <= 0) {
                    Toast.makeText(requireContext(), "La cantidad debe ser mayor que 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                receta.setCantidad(cantidad);
                receta.setUnidad(unidad);

                Toast.makeText(requireContext(), "Guardado: " + cantidad + " " + unidad, Toast.LENGTH_SHORT).show();
                dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.mostrarIngredientesSeleccionados();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                    .setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow()
                    .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}