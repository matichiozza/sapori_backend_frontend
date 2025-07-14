package com.example.sapori;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarContraseniaFragment_3 extends Fragment {

    private EditText etNuevaContrasenia, etRepetirContrasenia;
    private LinearLayout btnCambiarContrasenia;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recuperar_contrasenia_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etNuevaContrasenia = view.findViewById(R.id.et_nueva_contrasenia);
        etRepetirContrasenia = view.findViewById(R.id.et_repetir_contrasenia);
        btnCambiarContrasenia = view.findViewById(R.id.btn_cambiar_contrasenia);

        if (getArguments() != null) {
            email = getArguments().getString("email");
        }

        btnCambiarContrasenia.setOnClickListener(v -> {
            String nueva = etNuevaContrasenia.getText().toString().trim();
            String repetir = etRepetirContrasenia.getText().toString().trim();

            if (nueva.isEmpty() || repetir.isEmpty()) {
                Toast.makeText(getContext(), "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nueva.equals(repetir)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }

            cambiarContrasenia(nueva, email, view);
        });
    }

    private void cambiarContrasenia(String nuevaContrasenia, String email, View view) {
        ApiService apiService = ApiClient.getApiService();

        CambioContraseniaRequest request = new CambioContraseniaRequest(email, nuevaContrasenia);

        Call<ResponseBody> call = apiService.cambiarContrasenia(request);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_LONG).show();
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.nav_login);
                } else {
                    Toast.makeText(getContext(), "Error al cambiar contraseña", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
