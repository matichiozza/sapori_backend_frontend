package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private CheckBox checkboxRecordar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Referencias a vistas
        etEmail = view.findViewById(R.id.etLoginEmail);
        etPassword = view.findViewById(R.id.etContraseña1);
        checkboxRecordar = view.findViewById(R.id.checkbox_recordar);
        ImageView flecha = view.findViewById(R.id.flecha_login);
        View btnIniciarSesion = view.findViewById(R.id.rz1eoak6db9);
        TextView tvOlvideContrasenia = view.findViewById(R.id.texto_verificacion2); // NUEVO

        // Acción flecha: volver al inicio
        flecha.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    R.id.nav_inicio,
                    null,
                    new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_inicio, true)
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .build()
            );
        });

        tvOlvideContrasenia.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_login_to_fragment_recuperar_contrasenia_1);
        });


        // Acción iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> {
            if (validarCampos()) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", email);
                credentials.put("password", password);

                ApiService apiService = ApiClient.getApiService();
                Call<Usuario> call = apiService.login(credentials);

                call.enqueue(new Callback<Usuario>() {
                    @Override
                    public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Usuario usuario = response.body();
                            Toast.makeText(getContext(), "¡Bienvenido " + usuario.getAlias() + "!", Toast.LENGTH_SHORT).show();

                            // En onResponse cuando el login es exitoso:

                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

// Guardar datos del usuario siempre
                            editor.putString("nombre_usuario", usuario.getAlias());
                            editor.putString("email_usuario", usuario.getEmail());
                            editor.putString("nombre", usuario.getNombre());
                            editor.putString("apellido", usuario.getApellido());
                            editor.putLong("id_usuario", usuario.getId());


// Setear logueado siempre a true
                            editor.putBoolean("logueado", true);

// Guardar si quiere recordar (para usar luego al cerrar sesión)
                            editor.putBoolean("recordar_usuario", checkboxRecordar.isChecked());

                            editor.apply();


                            // Navegar al fragmento de bienvenida
                            Bundle bundle = new Bundle();
                            bundle.putString("nombre", usuario.getAlias());

                            Navigation.findNavController(v).navigate(
                                    R.id.action_nav_login_to_nav_inicio, bundle);
                        }
                        else if (response.code() == 403) {
                            Toast.makeText(getContext(), "Debes completar tu registro", Toast.LENGTH_LONG).show();
                        } else if (response.code() == 401) {
                            Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Error al iniciar sesión", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Usuario> call, Throwable t) {
                        Toast.makeText(getContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        return view;
    }

    private boolean validarCampos() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo es obligatorio");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria");
            return false;
        }

        return true;
    }
}
