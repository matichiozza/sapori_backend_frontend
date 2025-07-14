package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Alumno;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarCursosFragment extends Fragment {

    private LinearLayout tarjetaContainer;
    private EditText editBusqueda;
    private ImageButton btnFiltros;
    private ImageView flecha;

    private List<Map<String, Object>> listaCursosOriginal = new ArrayList<>();
    private String filtroBusquedaActual = "";
    private boolean esAlumno = false;

    public BuscarCursosFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buscar_cursos, container, false);

        tarjetaContainer = view.findViewById(R.id.tarjeta_container);
        editBusqueda = view.findViewById(R.id.edit_busqueda);
        flecha = view.findViewById(R.id.flecha);

        // Validación de usuario alumno
        SharedPreferences prefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);
        if (usuarioId == -1) {
            // No logueado, pero permitir ver cursos (solo como visitante)
            esAlumno = false;
            cargarCursos();
        } else {
            ApiService apiService = ApiClient.getApiService();
            apiService.obtenerAlumnoPorId(usuarioId).enqueue(new Callback<Alumno>() {
                @Override
                public void onResponse(Call<Alumno> call, Response<Alumno> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Es alumno
                        esAlumno = true;
                    } else {
                        // No es alumno
                        esAlumno = false;
                    }
                    cargarCursos();
                }
                @Override
                public void onFailure(Call<Alumno> call, Throwable t) {
                    // Error, asumir que no es alumno
                    esAlumno = false;
                    cargarCursos();
                }
            });
        }

        editBusqueda.setText(filtroBusquedaActual);

        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusquedaActual = s.toString();
                filtrarCursos(filtroBusquedaActual);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });

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

    private void cargarCursos() {
        ApiService apiService = ApiClient.getApiService();

        Call<List<Map<String, Object>>> call = apiService.obtenerCursosParaTarjetas();
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCursosOriginal = response.body();
                    filtrarCursos(filtroBusquedaActual);
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

    private void filtrarCursos(String filtro) {
        if (listaCursosOriginal == null) return;

        List<Map<String, Object>> cursosFiltrados = new ArrayList<>();
        for (Map<String, Object> curso : listaCursosOriginal) {
            String nombre = curso.get("nombre") != null ? curso.get("nombre").toString().toLowerCase() : "";
            if (nombre.contains(filtro.toLowerCase())) {
                cursosFiltrados.add(curso);
            }
        }

        mostrarCursos(cursosFiltrados);
    }

    private void mostrarCursos(List<Map<String, Object>> cursos) {
        if (!isAdded() || getContext() == null || tarjetaContainer == null) return;

        tarjetaContainer.removeAllViews();
        DecimalFormat formatoSinDecimal = new DecimalFormat("###.##");

        for (Map<String, Object> curso : cursos) {
            View tarjeta = LayoutInflater.from(getContext()).inflate(R.layout.fragment_tarjeta_curso, tarjetaContainer, false);

            Long cursoId = null;
            Object idObj = curso.get("id");
            if (idObj instanceof Number) cursoId = ((Number) idObj).longValue();

            String nombre = String.valueOf(curso.get("nombre"));
            String modalidad = String.valueOf(curso.get("modalidad"));

            String precioRaw = String.valueOf(curso.get("importe"));
            String precioFormateado;
            try {
                double precioDouble = Double.parseDouble(precioRaw);
                precioFormateado = formatoSinDecimal.format(precioDouble);
            } catch (Exception e) {
                precioFormateado = precioRaw;
            }

            Object duracionObj = curso.get("duracion");
            String duracion;
            if (duracionObj instanceof Number) {
                duracion = String.valueOf(((Number) duracionObj).intValue());
            } else {
                try {
                    duracion = String.valueOf((int) Double.parseDouble(String.valueOf(duracionObj)));
                } catch (Exception e) {
                    duracion = "-";
                }
            }

            String urlImagen = null;
            Object imagenObj = curso.get("fotoPrincipal");
            if (imagenObj instanceof List<?>) {
                List<?> imagenes = (List<?>) imagenObj;
                if (!imagenes.isEmpty() && imagenes.get(0) instanceof String) {
                    urlImagen = (String) imagenes.get(0);
                }
            }

            TextView titulo = tarjeta.findViewById(R.id.txt_nombre_curso);
            TextView modalidadTv = tarjeta.findViewById(R.id.modalidad);
            TextView precioTv = tarjeta.findViewById(R.id.txt_porciones);
            TextView duracionTv = tarjeta.findViewById(R.id.duracion);
            ImageView imagen = tarjeta.findViewById(R.id.imagen_curso);

            ImageView logoPresencial = tarjeta.findViewById(R.id.logo_presencial);
            ImageView logoNuevo = tarjeta.findViewById(R.id.logo_nuevo);
            TextView modalidad2 = tarjeta.findViewById(R.id.modalidad2);

            titulo.setText(nombre);
            modalidadTv.setText(modalidad);
            precioTv.setText(precioFormateado);
            duracionTv.setText(duracion);

            if (urlImagen != null && !urlImagen.isEmpty()) {
                Glide.with(requireContext())
                        .load(urlImagen.replace(" ", "%20"))
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imagen);
            } else {
                imagen.setImageResource(R.drawable.imagen_default);
            }

            String mod = modalidad != null ? modalidad.trim().toUpperCase() : "";
            if ("PRESENCIAL".equals(mod)) {
                logoPresencial.setVisibility(View.VISIBLE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
                modalidadTv.setVisibility(View.VISIBLE);
            } else if ("VIRTUAL".equals(mod)) {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.VISIBLE);
                modalidad2.setVisibility(View.VISIBLE);
                modalidadTv.setVisibility(View.GONE);
            } else {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
                modalidadTv.setVisibility(View.VISIBLE);
            }

            final Long finalCursoId = cursoId;
            tarjeta.setOnClickListener(v -> {
                if (finalCursoId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("cursoId", finalCursoId);

                    NavController navController = Navigation.findNavController(v);
                    if (esAlumno) {
                        navController.navigate(R.id.action_buscarCursosFragment_to_detalleCursoFragment, bundle);
                    } else {
                        navController.navigate(R.id.action_buscarCursosFragment_to_detalleCursoFragmentVisitante, bundle);
                    }
                } else {
                    Toast.makeText(getContext(), "ID de curso no disponible", Toast.LENGTH_SHORT).show();
                }
            });

            tarjetaContainer.addView(tarjeta);
        }
    }
}
