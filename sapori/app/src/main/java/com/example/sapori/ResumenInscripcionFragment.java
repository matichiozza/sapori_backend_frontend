package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.model.Curso;
import com.example.sapori.model.CursoSede;
import com.example.sapori.model.InscripcionRequest;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResumenInscripcionFragment extends Fragment {
    private static final String ARG_CURSO = "curso";
    private static final String ARG_SEDE = "sede";
    private static final String ARG_TARJETA = "tarjeta";
    private static final String ARG_PRECIO = "precio";
    private static final String ARG_PRECIO_ORIGINAL = "precioOriginal";
    private static final String ARG_DESCUENTO = "descuento";

    private Curso curso;
    private CursoSede cursoSede;
    private String numeroTarjeta;
    private float precio;
    private float precioOriginal;
    private int descuento;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen_inscripcion, container, false);

        if (getArguments() != null) {
            curso = (Curso) getArguments().getSerializable(ARG_CURSO);
            cursoSede = (CursoSede) getArguments().getSerializable(ARG_SEDE);
            numeroTarjeta = getArguments().getString(ARG_TARJETA, "");
            precio = getArguments().getFloat(ARG_PRECIO, 0f);
            precioOriginal = getArguments().getFloat(ARG_PRECIO_ORIGINAL, 0f);
            descuento = getArguments().getInt(ARG_DESCUENTO, 0);
        }

        TextView txtCurso = view.findViewById(R.id.txtCursoSeleccionado);
        TextView txtSede = view.findViewById(R.id.txtSedeSeleccionada);
        TextView txtTarjeta = view.findViewById(R.id.txtNumeroTarjeta);
        TextView txtPrecioFinal = view.findViewById(R.id.txtPrecioFinal);
        TextView txtPrecioOriginal = view.findViewById(R.id.txtPrecioOriginal);
        TextView txtDescuento = view.findViewById(R.id.txtDescuento);
        ImageView btnAtras = view.findViewById(R.id.btnAtras);
        TextView btnConfirmar = view.findViewById(R.id.btnConfirmarInscripcion);
        LinearLayout tarjetaContainer = view.findViewById(R.id.tarjeta_container);

        if (curso != null) txtCurso.setText(curso.getNombre());
        if (cursoSede != null) txtSede.setText(cursoSede.getSede().getNombre());
        if (numeroTarjeta != null && numeroTarjeta.length() >= 4) {
            String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);
            txtTarjeta.setText("**** **** **** " + ultimos4);
        } else {
            txtTarjeta.setText("**** **** **** 0000");
        }
        txtPrecioFinal.setText("$" + String.format("%.3f", precio));
        if (descuento > 0) {
            txtPrecioOriginal.setVisibility(View.VISIBLE);
            txtDescuento.setVisibility(View.VISIBLE);
            txtPrecioOriginal.setText("$" + String.format("%.0f", precioOriginal));
            txtDescuento.setText(descuento + "% OFF");
        } else {
            txtPrecioOriginal.setVisibility(View.GONE);
            txtDescuento.setVisibility(View.GONE);
        }

        btnAtras.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        btnConfirmar.setOnClickListener(v -> {
            // Obtener el ID del usuario desde SharedPreferences
            SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
            long usuarioId = userPrefs.getLong("id_usuario", -1);
            
            if (usuarioId == -1) {
                Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear la solicitud de inscripción
            InscripcionRequest inscripcionRequest = new InscripcionRequest(
                usuarioId, // El ID del usuario es el mismo que el del alumno
                curso.getId(),
                cursoSede.getId()
            );

            // Llamar al endpoint de inscripción
            ApiService apiService = ApiClient.getApiService();
            Call<Map<String, Object>> call = apiService.inscribirAlumnoEnCurso(inscripcionRequest);

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> responseData = response.body();
                        
                        // Verificar si se creó un pago
                        Boolean pagoCreado = (Boolean) responseData.get("pagoCreado");
                        if (pagoCreado != null && pagoCreado) {
                            // Mostrar mensaje sobre el pago creado
                            String mensajePago = "¡Inscripción exitosa! Se ha generado un pago por $" + 
                                String.format("%.2f", ((Number) responseData.get("importeFinal")).floatValue()) + 
                                " que vence en 30 días.";
                            Toast.makeText(requireContext(), mensajePago, Toast.LENGTH_LONG).show();
                        }
                        
                        // Inscripción exitosa, navegar a la pantalla de éxito
                        String duracion = "";
                        if (curso != null && curso.getDuracion() > 0) {
                            duracion = "- " + curso.getDuracion() + " semanas -";
                        }
                        Bundle args = new Bundle();
                        args.putString("curso", txtCurso.getText().toString());
                        args.putString("sede", txtSede.getText().toString());
                        args.putString("duracion", duracion);
                        Navigation.findNavController(view).navigate(R.id.action_resumenInscripcionFragment_to_inscripcionExitosaFragment, args);
                    } else {
                        // Manejar diferentes tipos de errores
                        String errorMessage = "Error al confirmar la inscripción";
                        
                        if (response.code() == 409) {
                            // Error de conflicto - usuario ya inscripto en el curso
                            if (response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMessage = "Ya estás inscripto en este curso. Solo puedes inscribirte una vez por curso.";
                                }
                            } else {
                                errorMessage = "Ya estás inscripto en este curso. Solo puedes inscribirte una vez por curso.";
                            }
                        } else if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                errorMessage = "Error al confirmar la inscripción";
                            }
                        }
                        
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }

    public static ResumenInscripcionFragment newInstance(Curso curso, CursoSede cursoSede, String numeroTarjeta, float precio, float precioOriginal, int descuento) {
        ResumenInscripcionFragment fragment = new ResumenInscripcionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURSO, curso);
        args.putSerializable(ARG_SEDE, cursoSede);
        args.putString(ARG_TARJETA, numeroTarjeta);
        args.putFloat(ARG_PRECIO, precio);
        args.putFloat(ARG_PRECIO_ORIGINAL, precioOriginal);
        args.putInt(ARG_DESCUENTO, descuento);
        fragment.setArguments(args);
        return fragment;
    }
} 