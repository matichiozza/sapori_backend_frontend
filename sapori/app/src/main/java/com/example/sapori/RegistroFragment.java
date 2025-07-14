package com.example.sapori;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroFragment extends Fragment {

    private static final String TAG = "RegistroFragment";

    private EditText etEmail, etAlias;
    private LinearLayout btnRegistrar;
    private TextView tvAliasError;

    private RecyclerView rvSugerenciasAlias;
    private SugerenciasAdapter sugerenciasAdapter;

    private ImageView btnBack;  // Imagen de la flecha para volver

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        etEmail = view.findViewById(R.id.et_email);
        etAlias = view.findViewById(R.id.et_alias);
        btnRegistrar = view.findViewById(R.id.botonRegistrarse);
        tvAliasError = view.findViewById(R.id.tv_alias_error);

        btnBack = view.findViewById(R.id.btn_back);  // Referencia a la flecha de volver

        tvAliasError.setVisibility(View.GONE);
        rvSugerenciasAlias = view.findViewById(R.id.rv_sugerencias_alias);
        rvSugerenciasAlias.setVisibility(View.GONE);

        // Configuro RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvSugerenciasAlias.setLayoutManager(layoutManager);
        rvSugerenciasAlias.setHasFixedSize(true);
        rvSugerenciasAlias.setNestedScrollingEnabled(true);

        sugerenciasAdapter = new SugerenciasAdapter();
        rvSugerenciasAlias.setAdapter(sugerenciasAdapter);

        // Listener para cuando se toca una sugerencia
        sugerenciasAdapter.setOnItemClickListener(sugerencia -> {
            etAlias.setText(sugerencia);
            rvSugerenciasAlias.setVisibility(View.GONE);
            tvAliasError.setVisibility(View.GONE);
        });

        // Divisor entre items del RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvSugerenciasAlias.getContext(), LinearLayoutManager.VERTICAL);
        rvSugerenciasAlias.addItemDecoration(dividerItemDecoration);

        // Oculto mensaje de error si usuario cambia el alias
        etAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAliasError.setVisibility(View.GONE);
                rvSugerenciasAlias.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnRegistrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String alias = etAlias.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Ingresá un email");
                return;
            }
            if (!esEmailValido(email)) {
                etEmail.setError("Ingresá un email válido");
                return;
            }
            if (TextUtils.isEmpty(alias)) {
                etAlias.setError("Ingresá un alias");
                return;
            }

            rvSugerenciasAlias.setVisibility(View.GONE);
            tvAliasError.setVisibility(View.GONE);

            verificarEmail(email, alias, view);
        });

        // Configuro el botón de volver atrás para que navegue hacia atrás
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                // Si no hay pantalla anterior, se puede cerrar el activity o hacer otra acción
                requireActivity().onBackPressed();
            }
        });
    }

    private boolean esEmailValido(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void verificarEmail(String email, String alias, View view) {
        ApiService apiService = ApiClient.getApiService();

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        Call<Map<String, String>> call = apiService.verificarEmail(body);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    verificarAlias(email, alias, view);
                } else if (response.code() == 409) {
                    Toast.makeText(getContext(), "Este email ya está registrado", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error inesperado verificarEmail: " + response.code());
                    Toast.makeText(getContext(), "Error al verificar email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "Fallo en verificarEmail", t);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarAlias(String email, String alias, View view) {
        ApiService apiService = ApiClient.getApiService();

        Map<String, String> body = new HashMap<>();
        body.put("alias", alias);

        Call<Map<String, Object>> call = apiService.verificarAlias(body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    tvAliasError.setVisibility(View.GONE);
                    iniciarRegistro(email, alias, view);
                } else if (response.code() == 409) {
                    tvAliasError.setVisibility(View.VISIBLE);

                    try {
                        String errorBodyStr = response.errorBody().string();
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>(){}.getType();
                        Map<String, Object> errorMap = gson.fromJson(errorBodyStr, type);

                        if (errorMap.containsKey("sugerencias")) {
                            @SuppressWarnings("unchecked")
                            List<String> sugerencias = (List<String>) errorMap.get("sugerencias");
                            if (!sugerencias.isEmpty()) {
                                sugerenciasAdapter.setSugerencias(sugerencias);
                                rvSugerenciasAlias.setVisibility(View.VISIBLE);
                            } else {
                                rvSugerenciasAlias.setVisibility(View.GONE);
                            }
                        } else {
                            rvSugerenciasAlias.setVisibility(View.GONE);
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Error leyendo errorBody", e);
                        Toast.makeText(getContext(), "Error de servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error inesperado verificarAlias: " + response.code());
                    Toast.makeText(getContext(), "Error al verificar alias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Fallo en verificarAlias", t);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarRegistro(String email, String alias, View view) {
        ApiService apiService = ApiClient.getApiService();

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setAlias(alias);

        Call<Usuario> call = apiService.iniciarRegistro(usuario);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "¡Registro iniciado! Revisá tu email",
                            Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("email", email);
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_nav_registro_to_fragment_confirmacion_codigo, bundle);

                } else if (response.code() == 409) {
                    Toast.makeText(getContext(),
                            "Alias o email ya está en uso",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error iniciarRegistro: " + response.code());
                    Toast.makeText(getContext(), "Error al iniciar registro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Log.e(TAG, "Fallo en iniciarRegistro", t);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}