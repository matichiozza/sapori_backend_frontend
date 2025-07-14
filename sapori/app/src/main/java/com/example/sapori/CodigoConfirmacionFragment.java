package com.example.sapori;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.model.ConfirmacionRequest;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodigoConfirmacionFragment extends Fragment {

    private EditText etCodigo;
    private LinearLayout btnEnviar;
    private TextView tvReenviar;
    private TextView tvTemporizador;

    private String email;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmacion_codigo, container, false);
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

        etCodigo = view.findViewById(R.id.et_codigo);
        btnEnviar = view.findViewById(R.id.btn_enviar_codigo);
        tvReenviar = view.findViewById(R.id.tv_reenviar_codigo);
        tvTemporizador = view.findViewById(R.id.tv_temporizador); // Asegurate de tener este TextView en el XML

        // Enviar código ingresado
        btnEnviar.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();
            if (TextUtils.isEmpty(codigo)) {
                etCodigo.setError("Ingresá el código");
                return;
            }

            ConfirmacionRequest request = new ConfirmacionRequest(email, codigo);
            ApiService apiService = ApiClient.getApiService();
            Call<ResponseBody> call = apiService.validarCodigo(request);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "¡Código correcto!", Toast.LENGTH_SHORT).show();

                        // Navegar al siguiente fragmento
                        NavController navController = Navigation.findNavController(view);
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        navController.navigate(R.id.action_fragment_confirmacion_codigo_to_fragment_completar_registro, bundle);

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

        // Reenviar código
        tvReenviar.setOnClickListener(v -> {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setAlias("temporal"); // valor dummy, backend lo ignora si ya existe

            ApiService apiService = ApiClient.getApiService();
            Call<Usuario> call = apiService.iniciarRegistro(usuario);

            tvReenviar.setEnabled(false); // deshabilita para evitar spam

            call.enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Código reenviado al email", Toast.LENGTH_SHORT).show();
                        iniciarTemporizador(); // empieza el contador
                    } else if (response.code() == 409) {
                        Toast.makeText(getContext(), "El registro ya fue completado", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error al reenviar código", Toast.LENGTH_SHORT).show();
                    }

                    // Reactivar botón en 30 segundos
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        tvReenviar.setEnabled(true);
                    }, 30000);
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        tvReenviar.setEnabled(true);
                    }, 30000);
                }
            });
        });
    }

    private void iniciarTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long millisEn30Min = 30 * 60 * 1000;

        countDownTimer = new CountDownTimer(millisEn30Min, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished / (60 * 1000);
                long segundos = (millisUntilFinished / 1000) % 60;

                String tiempo = String.format("Código válido por %02d:%02d minutos", minutos, segundos);
                tvTemporizador.setText(tiempo);
            }

            public void onFinish() {
                tvTemporizador.setText("El código ha expirado. Solicitá uno nuevo.");
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
