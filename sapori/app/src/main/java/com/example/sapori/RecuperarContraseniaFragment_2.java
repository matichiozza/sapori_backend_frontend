package com.example.sapori;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.sapori.model.ConfirmacionRequest;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarContraseniaFragment_2 extends Fragment {

    private EditText etCodigo;
    private LinearLayout btnEnviar;
    private TextView tvReenviar, tvTemporizador;
    private String email;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recuperar_contrasenia_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }

        if (email == null) {
            Toast.makeText(getContext(), "Error: email no disponible", Toast.LENGTH_LONG).show();
            return;
        }

        etCodigo = view.findViewById(R.id.et_codigo2);
        btnEnviar = view.findViewById(R.id.btn_enviar_codigo2);
        tvReenviar = view.findViewById(R.id.tv_reenviar_codigo2);
        tvTemporizador = view.findViewById(R.id.tv_temporizador2);

        btnEnviar.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();
            if (TextUtils.isEmpty(codigo)) {
                etCodigo.setError("Ingresá el código");
                return;
            }

            ConfirmacionRequest request = new ConfirmacionRequest(email, codigo);
            ApiService apiService = ApiClient.getApiService();
            Call<ResponseBody> call = apiService.validarCodigoRecuperacion(request); // Asegurate de que este endpoint exista

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "¡Código verificado!", Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        Navigation.findNavController(view).navigate(R.id.action_fragment_recuperar_contrasenia_2_to_fragment_recuperar_contrasenia_3, bundle);
                        Toast.makeText(getContext(), "Simulando navegación al fragmento de recuperación", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Código incorrecto o expirado", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void iniciarTemporizador() {
        if (countDownTimer != null) countDownTimer.cancel();

        long millis = 30 * 60 * 1000;
        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished / 60000;
                long segundos = (millisUntilFinished / 1000) % 60;
                tvTemporizador.setText(String.format("Código válido por %02d:%02d minutos", minutos, segundos));
            }

            public void onFinish() {
                tvTemporizador.setText("El código ha expirado.");
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
