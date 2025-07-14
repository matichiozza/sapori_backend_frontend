package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletarRegistroFragment extends Fragment {

    private EditText etNombre, etApellido, etPassword, etPasswordRepeat;
    private LinearLayout btnFinalizar;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datos_finales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etNombre = view.findViewById(R.id.et_nombre);
        etApellido = view.findViewById(R.id.et_apellido);
        etPassword = view.findViewById(R.id.et_password);
        etPasswordRepeat = view.findViewById(R.id.et_password_repeat);
        btnFinalizar = view.findViewById(R.id.btn_finalizar_registro);

        if (getArguments() != null) {
            email = getArguments().getString("email");
        }

        btnFinalizar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordRepeat = etPasswordRepeat.getText().toString().trim();

            if (email == null || email.isEmpty()) {
                Toast.makeText(getContext(), "Error: no se recibió el email", Toast.LENGTH_LONG).show();
                return;
            }

            if (nombre.isEmpty() || apellido.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordRepeat)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setPassword(password);

            ApiService apiService = ApiClient.getApiService();
            Call<Usuario> call = apiService.completarRegistro(usuario);

            call.enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario usuarioRespuesta = response.body();
                        Toast.makeText(getContext(), "¡Registro completo!", Toast.LENGTH_SHORT).show();

                        // Guardar en SharedPreferences
                        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombre_usuario", usuarioRespuesta.getAlias());
                        editor.putString("email_usuario", usuarioRespuesta.getEmail());
                        editor.putLong("id_usuario", usuarioRespuesta.getId());
                        editor.putString("nombre", usuarioRespuesta.getNombre());
                        editor.putString("apellido", usuarioRespuesta.getApellido());
                        editor.putBoolean("logueado", true);
                        editor.putBoolean("recordar_usuario", false); // por defecto, no recordar
                        editor.apply();

                        // Navegar al inicio con mensaje
                        Bundle bundle = new Bundle();
                        bundle.putString("nombre", usuarioRespuesta.getAlias());
                        Navigation.findNavController(view).navigate(R.id.nav_inicio, bundle);

                    } else {
                        try {
                            String errorMsg = response.errorBody().string();
                            Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error al finalizar el registro", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
