package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerSedesFragment extends Fragment {

    private LinearLayout tarjetaContainer;
    private ImageView flecha;
    private String filtroBusquedaActual = "";
    private List<Map<String, Object>> listaSedesOriginal = null; // renombrado
    private EditText editBusqueda;

    public VerSedesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ver_sedes, container, false);

        tarjetaContainer = view.findViewById(R.id.tarjeta_container);
        flecha = view.findViewById(R.id.flecha);
        editBusqueda = view.findViewById(R.id.edit_busqueda);

        // Validación de usuario alumno
        SharedPreferences prefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);
        if (usuarioId == -1) {
            // No logueado, acceso denegado
            Navigation.findNavController(view).navigate(R.id.fragment_acceso_denegado2);
            return view;
        }
        ApiService apiService = ApiClient.getApiService();
        apiService.obtenerAlumnoPorId(usuarioId).enqueue(new retrofit2.Callback<Alumno>() {
            @Override
            public void onResponse(Call<Alumno> call, Response<Alumno> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    // No es alumno, acceso denegado
                    Navigation.findNavController(view).navigate(R.id.fragment_acceso_denegado2);
                } else {
                    // Es alumno, continuar flujo normal
                    cargarSedes();
                }
            }
            @Override
            public void onFailure(Call<Alumno> call, Throwable t) {
                Navigation.findNavController(view).navigate(R.id.fragment_acceso_denegado2);
            }
        });

        // Setear texto actual al EditText para mantener filtro al volver
        editBusqueda.setText(filtroBusquedaActual);

        // Agregar listener para el texto
        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusquedaActual = s.toString();
                filtrarRecetas(filtroBusquedaActual);
            }
            @Override public void afterTextChanged(Editable s) {}
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

    private void filtrarRecetas(String filtro) {
        if (listaSedesOriginal == null) return;

        List<Map<String, Object>> filtradas = new ArrayList<>();

        for (Map<String, Object> sede : listaSedesOriginal) {
            String nombre = safeCastString(sede.get("nombre"), "").toLowerCase();
            if (nombre.contains(filtro.toLowerCase())) {
                filtradas.add(sede);
            }
        }

        mostrarSedes(filtradas);
    }

    private void cargarSedes() {
        ApiService apiService = ApiClient.getApiService();

        Call<List<Map<String, Object>>> call = apiService.obtenerSedesParaTarjetas();
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSedesOriginal = response.body();

                    // Si ya hay filtro, filtrar y mostrar
                    if (!filtroBusquedaActual.isEmpty()) {
                        filtrarRecetas(filtroBusquedaActual);
                    } else {
                        mostrarSedes(listaSedesOriginal);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al obtener sedes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarSedes(List<Map<String, Object>> sedes) {
        if (!isAdded() || getContext() == null || tarjetaContainer == null) return;

        tarjetaContainer.removeAllViews();

        for (Map<String, Object> sede : sedes) {
            Log.d("VerSedesFragment", "Sede: " + sede.toString());

            String nombre = String.valueOf(sede.get("nombre"));
            String telefono = String.valueOf(sede.get("telefono"));

            Object fotoPrincipalObj = sede.get("fotoPrincipal");
            String urlImagen = null;
            if (fotoPrincipalObj instanceof String) {
                urlImagen = (String) fotoPrincipalObj;
                Log.d("VerSedesFragment", "URL imagen seleccionada: " + urlImagen);
            }

            int cantidadCursosInt = 0;
            Object cantidadCursosObj = sede.get("cantidadCursos");
            if (cantidadCursosObj instanceof Number) {
                cantidadCursosInt = ((Number) cantidadCursosObj).intValue();
            }
            String cantidadCursos = cantidadCursosInt + " CURSOS";

            String zona = "-";
            if (sede.containsKey("zona")) {
                Object zonaObj = sede.get("zona");
                if (zonaObj instanceof String && !((String) zonaObj).trim().isEmpty()) {
                    zona = ((String) zonaObj).trim();
                }
            }

            View tarjeta = LayoutInflater.from(getContext()).inflate(R.layout.fragment_tarjeta_sede, tarjetaContainer, false);

            TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre_sede);
            TextView txtTelefono = tarjeta.findViewById(R.id.telefono);
            TextView txtCantidadCursos = tarjeta.findViewById(R.id.txt_porciones);
            TextView txtZona = tarjeta.findViewById(R.id.zona);
            ImageView imgSede = tarjeta.findViewById(R.id.imagen_curso);
            ImageView logoUbicacion = tarjeta.findViewById(R.id.logo_nuevo);

            txtNombre.setText(nombre != null ? nombre : "-");
            txtTelefono.setText(telefono != null ? telefono : "-");
            txtCantidadCursos.setText(cantidadCursos);
            txtZona.setText(zona);

            if (urlImagen != null && !urlImagen.isEmpty()) {
                Glide.with(requireContext())
                        .load(urlImagen.replace(" ", "%20"))
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imgSede);
            } else {
                imgSede.setImageResource(R.drawable.imagen_default);
            }

            tarjeta.setOnClickListener(v -> {
                Long sedeId = null;
                Object idObj = sede.get("id");
                if (idObj instanceof Number) {
                    sedeId = ((Number) idObj).longValue();
                }

                if (sedeId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("sedeId", sedeId);
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_verSedesFragment_to_detalleSedeFragment, bundle);
                } else {
                    Toast.makeText(getContext(), "ID de sede no disponible", Toast.LENGTH_SHORT).show();
                }
            });

            tarjetaContainer.addView(tarjeta);
        }
    }


    private String safeCastString(Object obj, String defaultVal) {
        return obj != null ? obj.toString() : defaultVal;
    }

}
