package com.example.sapori;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Receta;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarRecetasFragment extends Fragment {

    private LinearLayout tarjetaContainer;
    private EditText editBusqueda;
    private ImageButton btnFiltros;

    private List<Map<String, Object>> todasLasRecetas = new ArrayList<>();

    private String filtroBusquedaActual = "";
    private String filtroTipoSeleccionado = "";
    private String filtroAutorSeleccionado = "";
    private String filtroOrdenSeleccionado = "";

    private ImageView flecha;

    private static final String PREFS_NAME = "FiltrosPrefs";
    private static final String KEY_TIPO_SELECCIONADO = "tipo_seleccionado";
    private static final String KEY_AUTOR_SELECCIONADO = "autor_seleccionado";
    private static final String KEY_ORDEN_SELECCIONADO = "orden_seleccionado";

    private static final String PREFS_FAVORITOS = "FavoritosPrefs";
    private static final String KEY_FAVORITOS_SET = "favoritos_set";



    public BuscarRecetasFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buscar_recetas, container, false);

        getParentFragmentManager().setFragmentResultListener("actualizar_calificacion", getViewLifecycleOwner(), (requestKey, bundle) -> {
            boolean recargar = bundle.getBoolean("recargar_recetas", false);
            if (recargar) {
                cargarRecetas();
            }
        });

        tarjetaContainer = view.findViewById(R.id.tarjeta_container);
        editBusqueda = view.findViewById(R.id.edit_busqueda);
        btnFiltros = view.findViewById(R.id.btn_filtros);
        flecha = view.findViewById(R.id.flecha);

        // Cargar filtros previamente guardados
        cargarFiltrosGuardados();

        // Si no hay filtro de orden guardado, usar "AZ" por defecto
        if (filtroOrdenSeleccionado == null || filtroOrdenSeleccionado.isEmpty()) {
            filtroOrdenSeleccionado = "AZ";
        }

        // Establecer texto de búsqueda actual (si hay)
        editBusqueda.setText(filtroBusquedaActual);

        // Cargar recetas usando filtros actuales (incluye orden A-Z por defecto)
        cargarRecetas();

        // Escuchar cambios en el campo de búsqueda
        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusquedaActual = s.toString();
                filtrarRecetas(filtroBusquedaActual, filtroTipoSeleccionado, filtroAutorSeleccionado, filtroOrdenSeleccionado);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Botón de filtros
        btnFiltros.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_nav_buscar_recetas_to_fragment_filtrar_receta)
        );

        // Manejo de botón atrás (físico o gesto)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });

        // Flecha (volver)
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


    private void cargarFiltrosGuardados() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        filtroTipoSeleccionado = prefs.getString(KEY_TIPO_SELECCIONADO, "");
        filtroAutorSeleccionado = prefs.getString(KEY_AUTOR_SELECCIONADO, "");
        filtroOrdenSeleccionado = prefs.getString(KEY_ORDEN_SELECCIONADO, "");
    }

    private void cargarRecetas() {
        ApiService apiService = ApiClient.getApiService();

        String filtroOrden = filtroOrdenSeleccionado == null ? "" : filtroOrdenSeleccionado.trim().toUpperCase(Locale.ROOT);
        Log.d(TAG, "cargarRecetas - filtroOrden: " + filtroOrden);

        Call<List<Map<String, Object>>> call;

        switch (filtroOrden) {
            case "AZ":
                call = apiService.obtenerRecetasOrdenadasAlfabeticamente();
                break;
            case "RECIENTES":
                call = apiService.obtenerRecetasOrdenadasPorFechaDesc();
                break;
            case "ANTIGUAS":
                call = apiService.obtenerRecetasOrdenadasPorFechaAsc();
                break;
            default:
                call = apiService.obtenerRecetasParaTarjetas();
                break;
        }

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todasLasRecetas = response.body();

                    for (Map<String, Object> receta : todasLasRecetas) {
                        // Log completo de la receta
                        Log.d("DEBUG_RECETA", "Receta: " + receta.toString());

                        // Autor
                        Object autorObj = receta.get("autor");
                        if (autorObj instanceof Map) {
                            Map<String, Object> autorMap = (Map<String, Object>) autorObj;
                            Object alias = autorMap.get("alias");
                            if (alias != null) {
                                receta.put("autor", alias);
                            } else {
                                receta.put("autor", "Desconocido");
                            }
                        }

                        // Ingredientes (agregado)
                        Object ingredientes = receta.get("ingredientes");
                        if (ingredientes != null) {
                            Log.d("DEBUG_RECETA", "Ingredientes de receta ID " + receta.get("id") + ": " + ingredientes.toString());
                        } else {
                            Log.d("DEBUG_RECETA", "Receta ID " + receta.get("id") + " no tiene ingredientes.");
                        }

                        // Tipo
                        Log.d("DEBUG_RECETA", "Tipo: " + receta.get("tipo"));

                    }

                    filtrarRecetas(filtroBusquedaActual, filtroTipoSeleccionado, filtroAutorSeleccionado, filtroOrdenSeleccionado);
                } else {
                    Toast.makeText(requireContext(), "Error al obtener recetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void mostrarRecetas(List<Map<String, Object>> recetas) {
        if (!isAdded() || getContext() == null || getView() == null) return;

        SharedPreferences prefsAgregar = requireContext().getSharedPreferences("ingredientes_agregar_prefs", Context.MODE_PRIVATE);
        Set<String> originalesAgregar = prefsAgregar.getStringSet("ingredientes_agregar", new HashSet<>());
        Set<String> ingredientesSeleccionadosAgregar = new HashSet<>(originalesAgregar);

        SharedPreferences prefsQuitar = requireContext().getSharedPreferences("ingredientes_prefs", Context.MODE_PRIVATE);
        Set<String> originalesQuitar = prefsQuitar.getStringSet("ingredientes_seleccionados_a_quitar", new HashSet<>());
        Set<String> ingredientesSeleccionadosQuitar = new HashSet<>(originalesQuitar);

        if (tarjetaContainer != null) {
            tarjetaContainer.removeAllViews();
        }

        // 👉 Primero filtrar las recetas sin depender de favoritos
        List<Map<String, Object>> recetasFiltradas = new ArrayList<>();

        for (Map<String, Object> receta : recetas) {
            String nombreReceta = (String) receta.get("nombre");
            Object ingredientesObj = receta.get("ingredientes");

            if (!(ingredientesObj instanceof List)) {
                continue;
            }

            boolean coincide = recetaCoincideConIngredientes(receta, ingredientesSeleccionadosAgregar);
            boolean noContiene = recetaNoContieneIngredientes(receta, ingredientesSeleccionadosQuitar);

            if (coincide && noContiene) {
                recetasFiltradas.add(receta);
            }
        }

        // 👉 Luego obtener favoritos SOLO para marcar los IDs
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long userId = userPrefs.getLong("id_usuario", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            mostrarRecetasConFavoritos(recetasFiltradas, new HashSet<>());  // mostrar sin favoritos
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<Receta>> call = apiService.obtenerRecetasFavoritasPorId(userId);

        call.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                Set<Long> idsFavoritos = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Receta r : response.body()) {
                        idsFavoritos.add(r.getId());
                    }
                }
                mostrarRecetasConFavoritos(recetasFiltradas, idsFavoritos);  // ✅ Siempre se muestran las recetas filtradas
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                mostrarRecetasConFavoritos(recetasFiltradas, new HashSet<>());  // ✅ Igual que arriba
            }
        });
    }

    private void mostrarRecetasConFavoritos(List<Map<String, Object>> recetas, Set<Long> idsFavoritos) {
        if (!isAdded() || getContext() == null || getView() == null) return;

        // Ingredientes que deben estar
        SharedPreferences prefs1 = requireContext().getSharedPreferences("ingredientes_prefs", Context.MODE_PRIVATE);
        Set<String> ingredientesIncluir = prefs1.getStringSet("ingredientes_seleccionados", new HashSet<>());

        // Ingredientes que NO deben estar
        SharedPreferences prefsQuitar = requireContext().getSharedPreferences("ingredientes_quitar_prefs", Context.MODE_PRIVATE);
        Set<String> ingredientesExcluir = prefsQuitar.getStringSet("ingredientes_seleccionados", new HashSet<>());

        if (tarjetaContainer != null) {
            tarjetaContainer.removeAllViews();
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long userId = userPrefs.getLong("id_usuario", -1);

        SharedPreferences prefs = requireContext().getSharedPreferences("FavoritosPrefs_" + userId, Context.MODE_PRIVATE);

        for (Map<String, Object> receta : recetas) {
            // Aplicamos filtros de inclusión y exclusión
            if (!recetaCoincideConIngredientes(receta, ingredientesIncluir)) continue;
            if (!recetaNoContieneIngredientes(receta, ingredientesExcluir)) continue;

            View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_receta, tarjetaContainer, false);

            TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
            TextView txtAutor = tarjeta.findViewById(R.id.nombreautor);
            TextView txtCalificacion = tarjeta.findViewById(R.id.calificacion);
            TextView txtPorciones = tarjeta.findViewById(R.id.txt_porciones);
            TextView txtTiempo = tarjeta.findViewById(R.id.tiempo);
            TextView txtFecha = tarjeta.findViewById(R.id.txt_fecha);
            ImageView imagenReceta = tarjeta.findViewById(R.id.imagen_receta);
            ToggleButton btnFavorito = tarjeta.findViewById(R.id.btn_favorito);

            String nombre = safeCastString(receta.get("nombre"), "Sin nombre");
            String autor = safeCastString(receta.get("autor"), "Autor desconocido");
            String calificacion = safeCastString(receta.get("calificacion"), "N/D");
            String fotoPrincipal = safeCastString(receta.get("fotoPrincipal"), "");
            String fecha = safeCastString(receta.get("fecha"), "");
            String idReceta = String.valueOf(receta.get("id"));

            txtNombre.setText(nombre);
            txtAutor.setText(autor);
            txtCalificacion.setText(calificacion);
            txtPorciones.setText(parseIntString(receta.get("porciones"), "N/D"));

            String tiempoStr = parseIntString(receta.get("tiempo"), "N/D");
            txtTiempo.setText(tiempoStr.equals("N/D") ? tiempoStr : tiempoStr + "'");
            txtFecha.setText(fecha.isEmpty() ? "Fecha no disponible" : fecha);

            if (!fotoPrincipal.isEmpty()) {
                Glide.with(requireContext())
                        .load(encodeURL(fotoPrincipal))
                        .centerCrop()
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imagenReceta);
            } else {
                imagenReceta.setImageResource(R.drawable.imagen_default);
            }

            boolean esFavorito = false;
            try {
                long recetaIdLong = (long) Double.parseDouble(idReceta);
                esFavorito = idsFavoritos.contains(recetaIdLong);
            } catch (NumberFormatException e) {
                Log.e("Favoritos", "ID inválido: " + idReceta);
            }

            btnFavorito.setOnCheckedChangeListener(null);
            btnFavorito.setChecked(esFavorito);

            btnFavorito.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                ApiService apiService = ApiClient.getApiService();
                try {
                    long recetaIdLong = (long) Double.parseDouble(idReceta);
                    if (isChecked) {
                        apiService.agregarRecetaAFavoritos(userId, recetaIdLong).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!isAdded()) return;
                                if (response.isSuccessful()) {
                                    editor.putBoolean(idReceta, true).apply();
                                    Toast.makeText(requireContext(), nombre + " agregado a favoritos", Toast.LENGTH_SHORT).show();
                                } else {
                                    btnFavorito.setChecked(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                if (!isAdded()) return;
                                btnFavorito.setChecked(false);
                            }
                        });
                    } else {
                        apiService.eliminarRecetaDeFavoritos(userId, recetaIdLong).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!isAdded()) return;
                                if (response.isSuccessful()) {
                                    editor.remove(idReceta).apply();
                                    Toast.makeText(requireContext(), nombre + " removido de favoritos", Toast.LENGTH_SHORT).show();
                                } else {
                                    btnFavorito.setChecked(true);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                if (!isAdded()) return;
                                btnFavorito.setChecked(true);
                            }
                        });
                    }
                } catch (NumberFormatException e) {
                    Log.e("FavoritosAPI", "❌ Error al parsear idReceta: " + idReceta, e);
                }
            });

            txtAutor.setOnClickListener(v -> filtrarRecetasPorAutor(txtAutor.getText().toString()));

            Object idObj = receta.get("id");
            if (idObj != null) {
                try {
                    long recetaId = idObj instanceof Number
                            ? ((Number) idObj).longValue()
                            : (long) Double.parseDouble(idObj.toString());

                    tarjeta.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putLong("recetaId", recetaId);
                        Navigation.findNavController(v).navigate(R.id.action_nav_buscar_recetas_to_detalleRecetaFragment, bundle);
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "ID de receta inválido", Toast.LENGTH_SHORT).show();
                }
            }

            if (tarjetaContainer != null) {
                tarjetaContainer.addView(tarjeta);
            }
        }
    }





    private String parseIntString(Object obj, String defaultVal) {
        if (obj == null) return defaultVal;
        try {
            double val = Double.parseDouble(obj.toString());
            return String.valueOf((int) Math.round(val));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void filtrarRecetas(String texto, String tiposFiltroCSV, String autorFiltro, String orden) {
        List<Map<String, Object>> recetasFiltradas = new ArrayList<>();

        String textoLower = texto == null ? "" : texto.toLowerCase(Locale.ROOT).trim();
        String autorFiltroLower = autorFiltro == null ? "" : autorFiltro.toLowerCase(Locale.ROOT).trim();

        List<String> tiposSeleccionados = new ArrayList<>();
        if (tiposFiltroCSV != null && !tiposFiltroCSV.isEmpty()) {
            String[] tiposArray = tiposFiltroCSV.split(",");
            for (String tipo : tiposArray) {
                tiposSeleccionados.add(tipo.trim().toLowerCase(Locale.ROOT));
            }
        }

        for (Map<String, Object> receta : todasLasRecetas) {
            String nombre = safeCastString(receta.get("nombre"), "").toLowerCase(Locale.ROOT);
            String autor = safeCastString(receta.get("autor"), "").toLowerCase(Locale.ROOT);
            String tipo = safeCastString(receta.get("tipo"), "").toLowerCase(Locale.ROOT);

            boolean cumpleTipo = tiposSeleccionados.isEmpty() || tiposSeleccionados.contains(tipo);
            boolean cumpleTexto = textoLower.isEmpty() || nombre.contains(textoLower) || autor.contains(textoLower);
            boolean cumpleAutor = autorFiltroLower.isEmpty() || autor.contains(autorFiltroLower);

            if (cumpleTipo && cumpleTexto && cumpleAutor) {
                recetasFiltradas.add(receta);
            }
        }

        switch (orden.toUpperCase(Locale.ROOT)) {
            case "AZ":
                recetasFiltradas.sort(Comparator.comparing(r -> safeCastString(r.get("nombre"), "").toLowerCase(Locale.ROOT)));
                break;
            default:
                recetasFiltradas.sort(Comparator.comparing(r -> safeCastString(r.get("nombre"), "").toLowerCase(Locale.ROOT)));
                break;
            case "RECIENTES":
            case "ANTIGUAS":
                // No hacer nada, ya vienen ordenadas desde la API
                break;
        }

        mostrarRecetas(recetasFiltradas);
    }

    private void filtrarRecetasPorAutor(String autorBuscado) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putString(KEY_AUTOR_SELECCIONADO, autorBuscado).apply();

        filtroAutorSeleccionado = autorBuscado;

        filtrarRecetas(filtroBusquedaActual, filtroTipoSeleccionado, filtroAutorSeleccionado, filtroOrdenSeleccionado);
    }

    private void guardarFavorito(String recetaId, boolean esFavorito) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_FAVORITOS, 0);
        Set<String> favoritos = prefs.getStringSet(KEY_FAVORITOS_SET, new HashSet<>());
        // Crear un nuevo set para evitar ConcurrentModificationException
        Set<String> nuevosFavoritos = new HashSet<>(favoritos);

        if (esFavorito) {
            nuevosFavoritos.add(recetaId);
        } else {
            nuevosFavoritos.remove(recetaId);
        }

        prefs.edit().putStringSet(KEY_FAVORITOS_SET, nuevosFavoritos).apply();
    }


    private boolean recetaCoincideConIngredientes(Map<String, Object> receta, Set<String> ingredientesSeleccionados) {
        if (ingredientesSeleccionados.isEmpty()) {
            Log.d("FiltroIngredientes", "✅ No hay ingredientes seleccionados. Se acepta la receta.");
            return true;
        }

        Object ingredientesObj = receta.get("ingredientes");
        if (ingredientesObj == null) {
            Log.d("FiltroIngredientes", "❌ Ingredientes es null para receta ID " + receta.get("id"));
            return false;
        }

        Log.d("FiltroIngredientes", "Ingrediente raw para receta ID " + receta.get("id") + ": " + ingredientesObj.toString() + " (Clase: " + ingredientesObj.getClass().getName() + ")");

        if (!(ingredientesObj instanceof List)) {
            Log.d("FiltroIngredientes", "❌ La receta no tiene lista válida de ingredientes para receta ID " + receta.get("id"));
            return false;
        }

        List<?> ingredientesReceta = (List<?>) ingredientesObj;
        Set<String> idsIngredientesReceta = new HashSet<>();

        for (Object obj : ingredientesReceta) {
            if (obj instanceof Map) {
                Map<?, ?> ingMap = (Map<?, ?>) obj;
                Object ingredienteObj = ingMap.get("ingrediente");

                if (ingredienteObj instanceof Map) {
                    Map<?, ?> ingredienteMap = (Map<?, ?>) ingredienteObj;
                    Object idObj = ingredienteMap.get("id");

                    if (idObj != null) {
                        String idStr = String.valueOf(idObj).split("\\.")[0];
                        idsIngredientesReceta.add(idStr);
                    }
                }
            }
        }

        Log.d("FiltroIngredientes", "🧪 Ingredientes seleccionados: " + ingredientesSeleccionados);
        Log.d("FiltroIngredientes", "📋 Ingredientes de receta ID " + receta.get("id") + ": " + idsIngredientesReceta);

        boolean contieneTodos = idsIngredientesReceta.containsAll(ingredientesSeleccionados);
        Log.d("FiltroIngredientes", "🔍 Resultado para receta ID " + receta.get("id") + ": " + contieneTodos);

        return contieneTodos;
    }

    private boolean recetaNoContieneIngredientes(Map<String, Object> receta, Set<String> ingredientesSeleccionados) {
        if (ingredientesSeleccionados.isEmpty()) {
            // Si no hay ingredientes seleccionados, mostrar todas
            return true;
        }

        Object ingredientesObj = receta.get("ingredientes");
        if (!(ingredientesObj instanceof List)) return false;

        List<Map<String, Object>> ingredientesReceta = (List<Map<String, Object>>) ingredientesObj;

        for (Map<String, Object> ingMap : ingredientesReceta) {
            Object ingredienteObj = ingMap.get("ingrediente");
            if (ingredienteObj instanceof Map) {
                Map<String, Object> ingredienteMap = (Map<String, Object>) ingredienteObj;
                Object idObj = ingredienteMap.get("id");
                if (idObj != null) {
                    String idStr = String.valueOf(((Number) idObj).intValue());
                    Log.d("FiltroIngredientes", "Comparando ingrediente de receta con id=" + idStr);
                    if (ingredientesSeleccionados.contains(idStr)) {
                        Log.d("FiltroIngredientes", "Ingrediente está en lista de quitar, receta descartada.");
                        return false;
                    }
                }
            }
        }

        // Si pasó todos los ingredientes y no encontró ninguno de los seleccionados, la receta es válida
        return true;
    }

    private boolean esFavorito(String recetaId) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_FAVORITOS, 0);
        Set<String> favoritos = prefs.getStringSet(KEY_FAVORITOS_SET, new HashSet<>());
        return favoritos.contains(recetaId);
    }

    private String safeCastString(Object obj, String defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof String) return (String) obj;
        return obj.toString();
    }

    static String encodeURL(String url) {
        try {
            int index = url.indexOf("/img/");
            if (index == -1) return url;

            String base = url.substring(0, index + 5);
            String path = url.substring(index + 5);

            return base + URLEncoder.encode(path, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        cargarRecetas();  // refresca las recetas con la calificación actualizada
    }
}
