package com.example.sapori;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class CursosFragment extends Fragment {

    public CursosFragment() {
        super(R.layout.fragment_cursos); // este es tu XML ya creado
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!AuthUtils.estaLogueado(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.fragment_acceso_denegado);
            return;
        }

        ImageButton btnBuscarCursos = view.findViewById(R.id.img_fondo_recetas);
        btnBuscarCursos.setOnClickListener(v -> {
            // Reemplazá con el ID del fragment de búsqueda
            NavHostFragment.findNavController(this)
                    .navigate(R.id.fragment_buscar_cursos);
        });

        ImageView btnQr = view.findViewById(R.id.qr);
        btnQr.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.qrFragment);
        });

        // 🟢 Gestionar Cursos
        ImageButton btnGestionarCursos = view.findViewById(R.id.img_gestionar_recetas);
        btnGestionarCursos.setOnClickListener(v -> {
            // Reemplazá con el ID del fragment de gestión
            NavHostFragment.findNavController(this)
                    .navigate(R.id.fragment_mis_cursos);
        });

        // 🟢 Ver Sedes
        ImageButton btnVerSedes = view.findViewById(R.id.img_favoritos);
        btnVerSedes.setOnClickListener(v -> {
            // Reemplazá con el ID del fragment de sedes
            NavHostFragment.findNavController(this)
                    .navigate(R.id.fragment_ver_sedes);
        });
    }
}