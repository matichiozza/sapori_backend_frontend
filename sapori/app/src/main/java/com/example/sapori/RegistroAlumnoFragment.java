package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.sapori.model.Alumno;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroAlumnoFragment extends Fragment {

    private EditText etNombreCompleto, etTarjetaCredito, etFechaCaducidad, etCodigoSeguridad;

    public RegistroAlumnoFragment() {
        super(R.layout.fragment_registro_alumno);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombreCompleto = view.findViewById(R.id.et_nombre_completo);
        etTarjetaCredito = view.findViewById(R.id.et_tarjeta_credito);
        etFechaCaducidad = view.findViewById(R.id.et_fecha_caducidad);
        etCodigoSeguridad = view.findViewById(R.id.et_codigo_seguridad);

        // 👉 TextWatcher para formato MM/AA
        etFechaCaducidad.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^\\d]", ""); // solo dígitos
                if (input.equals(current)) return;

                String formatted = "";
                int inputLength = input.length();

                if (inputLength >= 2) {
                    formatted += input.substring(0, 2);
                    if (inputLength > 2) {
                        formatted += "/" + input.substring(2, Math.min(4, input.length()));
                    }
                } else {
                    formatted = input;
                }

                current = input;
                etFechaCaducidad.removeTextChangedListener(this);
                etFechaCaducidad.setText(formatted);
                etFechaCaducidad.setSelection(formatted.length());
                etFechaCaducidad.addTextChangedListener(this);
            }
        });

        // TextWatcher para formatear la tarjeta de crédito en grupos de 4 dígitos
        etTarjetaCredito.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > 16) digits = digits.substring(0, 16);
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }
                String result = formatted.toString();
                if (!result.equals(s.toString())) {
                    etTarjetaCredito.setText(result);
                    etTarjetaCredito.setSelection(result.length());
                }
                isFormatting = false;
            }
        });

        View btnContinuar = view.findViewById(R.id.btn_continuar_registro);
        btnContinuar.setOnClickListener(v -> guardarAlumnoParcialYAvanzar(v));
    }

    private void guardarAlumnoParcialYAvanzar(View view) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);

        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombreCompleto.getText().toString().trim();
        String tarjeta = etTarjetaCredito.getText().toString().trim().replaceAll("\\s", "");
        String fecha = etFechaCaducidad.getText().toString().trim();
        String codigo = etCodigoSeguridad.getText().toString().trim();

        if (nombre.isEmpty() || tarjeta.isEmpty() || fecha.isEmpty() || codigo.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación formato MM/AA
        if (!fecha.matches("\\d{2}/\\d{2}")) {
            Toast.makeText(requireContext(), "Fecha debe estar en formato MM/AA", Toast.LENGTH_SHORT).show();
            return;
        }

        Alumno alumno = new Alumno();
        alumno.setNombreCompleto(nombre);

        try {
            alumno.setNumTarjetaCredito(tarjeta);
            alumno.setCodigoSeguridad(Integer.parseInt(codigo));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Tarjeta o código inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        alumno.setFechaCaducidad(fecha);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        alumno.setUsuario(usuario);

        ApiService apiService = ApiClient.getApiService();
        Call<Alumno> call = apiService.crearAlumno1(alumno);

        call.enqueue(new Callback<Alumno>() {
            @Override
            public void onResponse(Call<Alumno> call, Response<Alumno> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Alumno creado correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_fragment_completar_registro_to_fragment_finalizar_registro2);
                } else if (response.code() == 409) {
                    Toast.makeText(requireContext(), "Este usuario ya tiene un alumno asociado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al crear alumno: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Alumno> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}