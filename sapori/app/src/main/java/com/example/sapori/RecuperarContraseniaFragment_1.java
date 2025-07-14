package com.example.sapori;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarContraseniaFragment_1 extends Fragment {

    private EditText etEmail;
    private LinearLayout btnRecuperar;
    private ImageView flechaVolver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recuperar_contrasenia_1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etEmail = view.findViewById(R.id.et_email_recuperacion);
        btnRecuperar = view.findViewById(R.id.btn_recuperar_contrasenia);
        flechaVolver = view.findViewById(R.id.flecha_volver);

        // Acción del botón recuperar
        btnRecuperar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Ingresá tu correo electrónico");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setAlias("temporal"); // Alias dummy, el backend lo ignora

            ApiService apiService = ApiClient.getApiService();
            Call<ResponseBody> call = apiService.enviarCodigoRecuperacion(usuario);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Correo enviado. Revisá tu email.", Toast.LENGTH_LONG).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_fragment_recuperar_contrasenia_1_to_fragment_recuperar_contrasenia_2, bundle);
                    } else {
                        Toast.makeText(getContext(), "Error al enviar email", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });

        // Acción del botón volver
        flechaVolver.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }
}
