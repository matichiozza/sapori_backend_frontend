package com.example.sapori;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class RecetasFragment extends Fragment {

    public RecetasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recetas, container, false);

        // Botón: Buscar Recetas
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnBuscar = view.findViewById(R.id.img_fondo_recetas);
        btnBuscar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_recetas_to_nav_buscar_recetas);

        });

        // Botón: Gestionar Recetas
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnGestionar = view.findViewById(R.id.img_gestionar_recetas);
        btnGestionar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_buscar_recetas_to_nav_gestionar_recetas);

        });

        // Botón: Mis Favoritos

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnFavoritos = view.findViewById(R.id.img_favoritos);
        btnFavoritos.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_recetas_to_nav_buscar_recetas_favoritas);

        });

        // Botón: Recetas Calculadas
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnCalculadas = view.findViewById(R.id.img_calculadas);
        btnCalculadas.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_nav_recetas_to_nav_recetas_calculadas)
        );

        return view;
    }
}