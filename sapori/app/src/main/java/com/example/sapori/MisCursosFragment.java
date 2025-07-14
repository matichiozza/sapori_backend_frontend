package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisCursosFragment extends Fragment {

    private ImageView flecha;
    private LinearLayout tarjetaContainer;
    private String filtroBusquedaActual = "";
    private List<Map<String, Object>> listaCursosOriginal = null;
    private EditText editBusqueda;

    public MisCursosFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mis_cursos, container, false);
        tarjetaContainer = view.findViewById(R.id.tarjeta_container);
        flecha = view.findViewById(R.id.flecha);
        editBusqueda = view.findViewById(R.id.edit_busqueda);

        editBusqueda.setText(filtroBusquedaActual);

        // Agregamos un TextWatcher para guardar y aplicar el filtro
        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusquedaActual = s.toString();
                filtrarRecetas(filtroBusquedaActual);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cargarCursos();

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }

    private void filtrarRecetas(String filtro) {
        if (listaCursosOriginal == null) return;
        List<Map<String, Object>> filtradas = new ArrayList<>();
        for (Map<String, Object> curso : listaCursosOriginal) {
            String nombre = safeCastString(curso.get("nombre"), "").toLowerCase();
            if (nombre.contains(filtro.toLowerCase())) {
                filtradas.add(curso);
            }
        }
        mostrarCursos(filtradas);
    }

    private void cargarCursos() {
        ApiService apiService = ApiClient.getApiService();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long alumnoId = sharedPreferences.getLong("id_usuario", -1);

        if (alumnoId == -1) {
            Toast.makeText(requireContext(), "Error: No se encontrÃ³ el ID del usuario", Toast.LENGTH_SHORT).show();
            Log.e("MisCursosFragment", "ID de usuario no encontrado en SharedPreferences");
            return;
        }

        Call<List<Map<String, Object>>> call = apiService.getCursosPorAlumno(alumnoId);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCursosOriginal = response.body(); // Guardamos todos
                    filtrarRecetas(filtroBusquedaActual); // Aplicamos el filtro
                } else if (response.code() == 204) {
                    Toast.makeText(requireContext(), "No hay cursos inscriptos.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al obtener cursos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCursos(List<Map<String, Object>> cursos) {
        if (!isAdded() || getContext() == null || tarjetaContainer == null) return;
        tarjetaContainer.removeAllViews();

        for (Map<String, Object> curso : cursos) {
            String nombreCurso = String.valueOf(curso.get("nombre"));
            String modalidad = String.valueOf(curso.get("modalidad"));
            String cursadaHorarioDia = String.valueOf(curso.get("horarioCursada"));
            String imagenUrl = curso.get("imagenUrl") != null ? String.valueOf(curso.get("imagenUrl")) : null;
            String estadoCurso = curso.get("estadoCurso") != null ? String.valueOf(curso.get("estadoCurso")).toLowerCase() : "";

            View tarjeta = LayoutInflater.from(getContext()).inflate(R.layout.fragment_tarjeta_mis_cursos, tarjetaContainer, false);

            TextView txtNombreCurso = tarjeta.findViewById(R.id.txt_nombre_curso);
            TextView txtModalidad = tarjeta.findViewById(R.id.modalidad);
            TextView txtDuracion = tarjeta.findViewById(R.id.duracion);
            ImageView imgCurso = tarjeta.findViewById(R.id.imagen_curso);
            ImageView puntoVerde = tarjeta.findViewById(R.id.punto_verde);
            ImageView puntoRojo = tarjeta.findViewById(R.id.punto_rojo);
            ImageView puntoAmarillo = tarjeta.findViewById(R.id.punto_amarillo);
            TextView txtEnCurso = tarjeta.findViewById(R.id.txt_en_curso);
            TextView txtFinalizado = tarjeta.findViewById(R.id.txt_en_curso2);
            TextView txtProximamente = tarjeta.findViewById(R.id.txt_en_curso1);
            ImageView logoPresencial = tarjeta.findViewById(R.id.logo_presencial);
            ImageView logoNuevo = tarjeta.findViewById(R.id.logo_nuevo);
            TextView modalidad2 = tarjeta.findViewById(R.id.modalidad2);

            txtNombreCurso.setText(!TextUtils.isEmpty(nombreCurso) ? nombreCurso : "-");
            txtModalidad.setText(!TextUtils.isEmpty(modalidad) ? modalidad : "-");
            txtDuracion.setText(!TextUtils.isEmpty(cursadaHorarioDia) ? cursadaHorarioDia : "-");

            if (!TextUtils.isEmpty(imagenUrl)) {
                Glide.with(MisCursosFragment.this)
                        .load(imagenUrl.replace(" ", "%20"))
                        .centerCrop()
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imgCurso);
            } else {
                imgCurso.setImageResource(R.drawable.imagen_default);
            }

            String mod = capitalize(modalidad != null ? modalidad.trim() : "");
            if (mod.equals("Presencial")) {
                logoPresencial.setVisibility(View.VISIBLE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
                txtModalidad.setVisibility(View.VISIBLE);
            } else if (mod.equals("Virtual")) {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.VISIBLE);
                modalidad2.setVisibility(View.VISIBLE);
            } else {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
            }

            puntoVerde.setVisibility(View.GONE);
            puntoRojo.setVisibility(View.GONE);
            puntoAmarillo.setVisibility(View.GONE);
            txtEnCurso.setVisibility(View.GONE);
            txtFinalizado.setVisibility(View.GONE);
            txtProximamente.setVisibility(View.GONE);

            switch (estadoCurso) {
                case "en_curso":
                    puntoVerde.setVisibility(View.VISIBLE);
                    txtEnCurso.setVisibility(View.VISIBLE);
                    break;
                case "finalizado":
                    puntoRojo.setVisibility(View.VISIBLE);
                    txtFinalizado.setVisibility(View.VISIBLE);
                    break;
                case "proximamente":
                    puntoAmarillo.setVisibility(View.VISIBLE);
                    txtProximamente.setVisibility(View.VISIBLE);
                    break;
            }

            Object idObj = curso.get("cursoId");
            Long cursoId = null;
            if (idObj instanceof Number) {
                cursoId = ((Number) idObj).longValue();
            } else if (idObj != null) {
                try {
                    cursoId = Long.parseLong(idObj.toString());
                } catch (NumberFormatException e) {
                    Log.e("MostrarCursos", "Error convirtiendo id a long", e);
                }
            }

            final Long finalCursoId = cursoId;
            tarjeta.setOnClickListener(v -> {
                if (finalCursoId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("cursoId", finalCursoId);
                    bundle.putString("nombre", nombreCurso);
                    bundle.putString("modalidad", modalidad);
                    bundle.putString("horario", cursadaHorarioDia);
                    bundle.putString("imagenUrl", imagenUrl);
                    bundle.putString("estadoCurso", estadoCurso);

                    NavHostFragment.findNavController(MisCursosFragment.this)
                            .navigate(R.id.detalleCursoFragment2, bundle);
                } else {
                    Toast.makeText(getContext(), "ID de curso no disponible", Toast.LENGTH_SHORT).show();
                }
            });

            tarjetaContainer.addView(tarjeta);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String safeCastString(Object obj, String defaultVal) {
        return obj != null ? obj.toString() : defaultVal;
    }
}
