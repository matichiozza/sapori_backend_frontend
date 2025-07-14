package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Receta;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionarRecetasFragment extends Fragment {

    private static final String TAG = "GestionarRecetasFragment";

    private final Gson gson = new Gson();
    private ImageView flecha;
    private String filtroBusquedaActual = "";
    private List<Map<String, Object>> recetasFiltradas = null;

    private EditText editBusqueda;

    private LinearLayout tarjetaContainer;
    private final List<View> tarjetasSeleccionadas = new ArrayList<>();
    private boolean modoSeleccion = false;

    public GestionarRecetasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestionar_recetas, container, false);

        tarjetaContainer = view.findViewById(R.id.tarjeta_container_gestionar_recetas);
        flecha = view.findViewById(R.id.flecha);
        editBusqueda = view.findViewById(R.id.edit_busqueda);
        FrameLayout circuloVerde = view.findViewById(R.id.circulo_verde);
        ImageView iconoMas = view.findViewById(R.id.icono_dentro_circulo);
        FrameLayout circulo_verde = view.findViewById(R.id.circulo_verde);
        circulo_verde.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_gestionarRecetas_to_agregarReceta);
        });
        iconoMas.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_gestionarRecetas_to_agregarReceta);
        });
        editBusqueda.setText(filtroBusquedaActual);

        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusquedaActual = s.toString();
                filtrarRecetas(filtroBusquedaActual);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        ImageView iconoCheck = view.findViewById(R.id.iconocheck2);
        iconoCheck.setTag("unchecked");

        iconoCheck.setOnClickListener(v -> {
            Object tag = iconoCheck.getTag();

            if ("checked".equals(tag)) {
                // Ya estamos en modo selección
                if (tarjetasSeleccionadas.isEmpty()) {
                    // No hay recetas seleccionadas, entonces "cancelamos" modo selección:
                    iconoCheck.setImageResource(R.drawable.iconocheck2);
                    iconoCheck.setTag("unchecked");
                    circuloVerde.setVisibility(View.VISIBLE);
                    modoSeleccion = false;
                    limpiarSeleccion();
                    Toast.makeText(getContext(), "Modo selección cancelado", Toast.LENGTH_SHORT).show();
                    return;  // No hacemos más nada
                }

                // Si hay recetas seleccionadas, procedemos a borrar
                borrarRecetasSeleccionadas();

                // Volver a modo normal después de borrar
                iconoCheck.setImageResource(R.drawable.iconocheck2);
                iconoCheck.setTag("unchecked");
                circuloVerde.setVisibility(View.VISIBLE);
                modoSeleccion = false;
                limpiarSeleccion();

            } else {
                // No estamos en modo selección: activar modo selección para eliminar
                iconoCheck.setImageResource(R.drawable.iconotacho);
                iconoCheck.setTag("checked");
                circuloVerde.setVisibility(View.GONE);
                modoSeleccion = true;
                Toast.makeText(getContext(), "Selecciona recetas para eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        String alias = sharedPreferences.getString("nombre_usuario", null);

        if (alias != null && !alias.trim().isEmpty()) {
            cargarRecetasPorAlias(alias.trim());
        } else {
            Toast.makeText(requireContext(), "Alias no especificado", Toast.LENGTH_SHORT).show();
            tarjetaContainer.removeAllViews();
        }

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
        if (recetasFiltradas == null) return;

        List<Map<String, Object>> filtradas = new ArrayList<>();

        for (Map<String, Object> receta : recetasFiltradas) {
            String nombre = safeCastString(receta.get("nombre"), "").toLowerCase();
            if (nombre.contains(filtro.toLowerCase())) {
                filtradas.add(receta);
            }
        }

        mostrarTarjetas(filtradas);
    }



    private void borrarRecetasSeleccionadas() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        String alias = sharedPreferences.getString("nombre_usuario", null);

        if (alias == null || alias.trim().isEmpty()) {
            Toast.makeText(getContext(), "Alias no disponible para eliminar recetas", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        AtomicInteger eliminadas = new AtomicInteger(0);
        int totalSeleccionadas = tarjetasSeleccionadas.size();

        for (View tarjeta : new ArrayList<>(tarjetasSeleccionadas)) {
            TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
            String nombreReceta = txtNombre.getText().toString();

            Object tagObj = tarjeta.getTag();
            if (tagObj == null) {
                Log.e(TAG, "No se encontró ID de receta en la tarjeta");
                eliminadas.incrementAndGet();
                continue;
            }
            Long idReceta;
            try {
                if (tagObj instanceof Long) {
                    idReceta = (Long) tagObj;
                } else if (tagObj instanceof String) {
                    // Por si llegara como string
                    idReceta = Long.parseLong((String) tagObj);
                } else {
                    idReceta = Long.parseLong(tagObj.toString());
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "ID de receta inválido: " + tagObj);
                eliminadas.incrementAndGet();
                continue;
            }

            apiService.eliminarRecetaPorAlias(alias.trim(), idReceta).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        tarjetaContainer.removeView(tarjeta);
                        tarjetasSeleccionadas.remove(tarjeta);
                        if (eliminadas.incrementAndGet() == totalSeleccionadas) {
                            Toast.makeText(getContext(), "Recetas eliminadas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al eliminar receta \"" + nombreReceta + "\"", Toast.LENGTH_SHORT).show();
                        if (eliminadas.incrementAndGet() == totalSeleccionadas) {
                            Toast.makeText(getContext(), "Operación finalizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Fallo al eliminar receta \"" + nombreReceta + "\": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (eliminadas.incrementAndGet() == totalSeleccionadas) {
                        Toast.makeText(getContext(), "Operación finalizada", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void cargarRecetasPorAlias(String alias) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.obtenerRecetasPorAlias(alias);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> recetas = response.body();
                    recetasFiltradas = recetas;
                    if (!recetas.isEmpty()) {
                        recetasFiltradas = recetas; // Guardamos TODAS las recetas
                        if (filtroBusquedaActual != null && !filtroBusquedaActual.isEmpty()) {
                            filtrarRecetas(filtroBusquedaActual); // Aplicamos el filtro si hay uno
                        } else {
                            mostrarTarjetas(recetasFiltradas); // Si no hay filtro, mostramos todas
                        }
                    } else {
                        Toast.makeText(requireContext(), "No se encontraron recetas", Toast.LENGTH_SHORT).show();
                        tarjetaContainer.removeAllViews();
                    }
                } else {
//                    Toast.makeText(requireContext(), "Error al cargar recetas", Toast.LENGTH_SHORT).show();
                    tarjetaContainer.removeAllViews();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tarjetaContainer.removeAllViews();
            }
        });
    }

    private void mostrarTarjetas(List<Map<String, Object>> recetasFavoritas) {
        new Handler(Looper.getMainLooper()).post(() -> {
            tarjetaContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            for (Map<String, Object> receta : recetasFavoritas) {
                Log.e("TarjetasReceta", "Contenido completo de la receta: " + receta.toString());

                View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_receta, tarjetaContainer, false);

                TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
                TextView txtAutor = tarjeta.findViewById(R.id.nombreautor);
                TextView txtCalificacion = tarjeta.findViewById(R.id.calificacion);
                TextView txtPorciones = tarjeta.findViewById(R.id.txt_porciones);
                TextView txtTiempo = tarjeta.findViewById(R.id.tiempo);
                TextView txtFecha = tarjeta.findViewById(R.id.txt_fecha);
                ImageView imagenReceta = tarjeta.findViewById(R.id.imagen_receta);
                ToggleButton btnFavorito = tarjeta.findViewById(R.id.btn_favorito);
                View botonVerde1 = tarjeta.findViewById(R.id.botonVerde);

                if (btnFavorito != null) btnFavorito.setVisibility(View.GONE);
                if (botonVerde1 != null) botonVerde1.setVisibility(View.GONE);

                String nombre = safeCastString(receta.get("nombre"), "Sin nombre");
                String aliasAutor = "Autor desconocido";
                if (receta.get("autor") instanceof Map) {
                    Object aliasObj = ((Map<String, Object>) receta.get("autor")).get("alias");
                    if (aliasObj != null) aliasAutor = aliasObj.toString();
                }

                String calificacion = safeCastString(receta.get("calificacion"), "N/D");
                int porcionesInt = parseInt(receta.get("porciones"));
                int tiempoInt = parseInt(receta.get("tiempo"));
                String fecha = safeCastString(receta.get("fechaCreacion"), "Fecha no disponible");
                String fotoPrincipal = safeCastString(receta.get("fotoPrincipal"), "");

                long recetaId = -1;
                try {
                    Object idObj = receta.get("id");
                    if (idObj instanceof Number) {
                        recetaId = ((Number) idObj).longValue();
                    } else if (idObj instanceof String) {
                        recetaId = Long.parseLong((String) idObj);
                    } else if (idObj != null) {
                        recetaId = (long) Double.parseDouble(idObj.toString());
                    }
                    Log.e("TarjetasReceta", "✅ ID extraído: " + recetaId);
                } catch (Exception e) {
                    Log.e("TarjetasReceta", "❌ Error al obtener ID de receta: " + e.getMessage());
                }

                txtNombre.setText(nombre);
                txtAutor.setText(aliasAutor);
                txtCalificacion.setText(calificacion);
                txtPorciones.setText(String.valueOf(porcionesInt));
                txtTiempo.setText(tiempoInt > 0 ? tiempoInt + "'" : "N/D");
                txtFecha.setText(fecha);

                if (!fotoPrincipal.isEmpty()) {
                    Glide.with(requireContext())
                            .load(fotoPrincipal)
                            .centerCrop()
                            .placeholder(R.drawable.imagen_default)
                            .error(R.drawable.imagen_default)
                            .into(imagenReceta);
                } else {
                    imagenReceta.setImageResource(R.drawable.imagen_default);
                }

                long finalRecetaId = recetaId;
                tarjeta.setTag(finalRecetaId);

                tarjeta.setOnClickListener(v -> {
                    Log.d("TarjetasReceta", "Click tarjeta, recetaId: " + finalRecetaId);

                    if (modoSeleccion) {
                        View overlaySeleccion = v.findViewById(R.id.overlay_seleccion);
                        View seleccionada = v.findViewById(R.id.txt_seleccionada);
                        View icono_seleccion = v.findViewById(R.id.icono_seleccion);
                        ToggleButton btnFavoritoLocal = v.findViewById(R.id.btn_favorito);
                        View botonVerde = v.findViewById(R.id.botonVerde);

                        if (tarjetasSeleccionadas.contains(v)) {
                            tarjetasSeleccionadas.remove(v);
                            if (overlaySeleccion != null) overlaySeleccion.setVisibility(View.GONE);
                            if (seleccionada != null) seleccionada.setVisibility(View.GONE);
                            if (icono_seleccion != null) icono_seleccion.setVisibility(View.GONE);
                            if (btnFavoritoLocal != null) btnFavoritoLocal.setVisibility(View.VISIBLE);
                            if (botonVerde != null) botonVerde.setVisibility(View.VISIBLE);
                        } else {
                            tarjetasSeleccionadas.add(v);
                            if (overlaySeleccion != null) overlaySeleccion.setVisibility(View.VISIBLE);
                            if (seleccionada != null) seleccionada.setVisibility(View.VISIBLE);
                            if (icono_seleccion != null) icono_seleccion.setVisibility(View.VISIBLE);
                            if (btnFavoritoLocal != null) btnFavoritoLocal.setVisibility(View.GONE);
                            if (botonVerde != null) botonVerde.setVisibility(View.GONE);
                        }

                    } else {
                        if (finalRecetaId > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putLong("recetaId", finalRecetaId);
                            bundle.putBoolean("desdeGestionar", true);

                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_gestionarRecetas_to_detalleRecetaFragment, bundle);
                        } else {
                            Toast.makeText(getContext(), "No se puede abrir la receta: ID inválido", Toast.LENGTH_SHORT).show();
                            Log.e("TarjetasReceta", "❌ ID inválido: " + finalRecetaId);
                        }
                    }
                });

                tarjetaContainer.addView(tarjeta);
            }
        });
    }



    private void limpiarSeleccion() {
        for (View tarjeta : tarjetasSeleccionadas) {
            tarjeta.setBackgroundResource(0);
            // Eliminado visibilidad de btnFavorito porque ya no existe.
        }
        tarjetasSeleccionadas.clear();
    }


    private String safeCastString(Object obj, String defaultVal) {
        return obj != null ? obj.toString() : defaultVal;
    }

    private int parseInt(Object obj) {
        if (obj == null) return 0;
        try {
            return (int) Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (filtroBusquedaActual != null && !filtroBusquedaActual.isEmpty()) {
            filtrarRecetas(filtroBusquedaActual);
        }
    }

}