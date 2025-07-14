package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AjustesFragment extends Fragment {

    public AjustesFragment() {
        super(R.layout.fragment_ajustes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⚠️ VERIFICAR ACCESO
        if (!AuthUtils.estaLogueado(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.fragment_acceso_denegado);
            return;
        }

        // Obtener referencias a los botones
        View btnPerfil = view.findViewById(R.id.btn_perfil);
        View btnCambiarContrasenia = view.findViewById(R.id.btn_cambiar_contrasenia_2);
        View btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion);
        View btnCambiarRol = view.findViewById(R.id.btn_cambiar_rol);
        View btnCuentaCorriente = view.findViewById(R.id.btn_cuenta_corriente);

        // Ocultar ambos botones por defecto
        btnCambiarRol.setVisibility(View.GONE);
        btnCuentaCorriente.setVisibility(View.GONE);

        // Obtener usuario logueado desde SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);

        if (usuarioId != -1) {
            ApiService apiService = ApiClient.getApiService();
            Call<Usuario> call = apiService.obtenerUsuarioPorId(usuarioId);

            call.enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario usuario = response.body();
                        if (usuario.isEsAlumno()) {
                            btnCuentaCorriente.setVisibility(View.VISIBLE);
                        } else {
                            btnCambiarRol.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener usuario", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Acción botones
        btnPerfil.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_ajustes_to_datosPersonales);
        });

        btnCambiarContrasenia.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_ajustes_to_cambiarContrasenia);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            NavController navController = NavHostFragment.findNavController(this);
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            navController.navigate(R.id.nav_inicio, null, navOptions);

            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });

        btnCambiarRol.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_ajustes_to_registroAlumno);
        });

        btnCuentaCorriente.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_ajustes_to_cuentaCorrienteFragment);
        });
    }
}