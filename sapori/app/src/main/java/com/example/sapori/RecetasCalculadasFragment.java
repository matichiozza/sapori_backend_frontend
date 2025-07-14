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
import com.example.sapori.model.RecetaCalculadaDTO;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecetasCalculadasFragment extends Fragment {

    private static final String TAG = "RecetasCalculadas";

    private ImageView flecha;
    private EditText editBusqueda;
    private String filtroBusquedaActual = "";
    private LinearLayout tarjetaContainer;
    private boolean modoSeleccion = false;
    private final List<View> tarjetasSeleccionadas = new ArrayList<>();
    private List<RecetaCalculadaDTO> todasLasRecetas = new ArrayList<>();

    public RecetasCalculadasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recetas_calculadas, container, false);

        editBusqueda = view.findViewById(R.id.edit_busqueda);
        tarjetaContainer = view.findViewById(R.id.tarjeta_container_calculadas);
        flecha = view.findViewById(R.id.flecha);
        ImageView iconoCheck = view.findViewById(R.id.iconocheck2);
        iconoCheck.setTag("unchecked");

        iconoCheck.setOnClickListener(v -> {
            Object tag = iconoCheck.getTag();

            if ("checked".equals(tag)) {
                if (tarjetasSeleccionadas.isEmpty()) {
                    iconoCheck.setImageResource(R.drawable.iconocheck2);
                    iconoCheck.setTag("unchecked");
                    modoSeleccion = false;
                    limpiarSeleccion();
                    Toast.makeText(getContext(), "Modo selección cancelado", Toast.LENGTH_SHORT).show();
                    return;
                }
                borrarRecetasSeleccionadas();
                iconoCheck.setImageResource(R.drawable.iconocheck2);
                iconoCheck.setTag("unchecked");
                modoSeleccion = false;
                limpiarSeleccion();
            } else {
                iconoCheck.setImageResource(R.drawable.iconotacho);
                iconoCheck.setTag("checked");
                modoSeleccion = true;
                Toast.makeText(getContext(), "Selecciona recetas para eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        editBusqueda.setText(filtroBusquedaActual);  // Para restaurar el texto cuando volvés del detalle

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

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        cargarDatosDeRecetasCalculadas();
        return view;
    }

    private void cargarDatosDeRecetasCalculadas() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = sharedPreferences.getLong("id_usuario", -1);

        if (usuarioId == -1) {
            Toast.makeText(getContext(), "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ID de usuario no encontrado en SharedPreferences.");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<RecetaCalculadaDTO>> call = apiService.getRecetasCalculadas(usuarioId);

        call.enqueue(new Callback<List<RecetaCalculadaDTO>>() {
            @Override
            public void onResponse(Call<List<RecetaCalculadaDTO>> call, Response<List<RecetaCalculadaDTO>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<RecetaCalculadaDTO> recetas = response.body();
                    if (recetas.isEmpty()) {
                        Toast.makeText(getContext(), "No tienes recetas calculadas guardadas.", Toast.LENGTH_SHORT).show();
                        tarjetaContainer.removeAllViews();
                    } else {
                        todasLasRecetas = recetas;
                        if (!filtroBusquedaActual.isEmpty()) {
                            filtrarRecetas(filtroBusquedaActual);
                        } else {
                            mostrarTarjetasDeRecetas(todasLasRecetas);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener las recetas", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al obtener recetas: Código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RecetaCalculadaDTO>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Fallo en la conexión", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Fallo en la API", t);
            }
        });
    }

    private void filtrarRecetas(String filtro) {
        if (todasLasRecetas == null) return;

        List<RecetaCalculadaDTO> filtradas = new ArrayList<>();
        for (RecetaCalculadaDTO receta : todasLasRecetas) {
            if (receta.getNombre() != null &&
                    receta.getNombre().toLowerCase().contains(filtro.toLowerCase())) {
                filtradas.add(receta);
            }
        }
        mostrarTarjetasDeRecetas(filtradas);
    }

    private void mostrarTarjetasDeRecetas(List<RecetaCalculadaDTO> recetasCalculadas) {
        if (!isAdded() || getContext() == null || getView() == null) return;

        tarjetaContainer.removeAllViews();
        tarjetasSeleccionadas.clear();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Limitar la cantidad a 10 recetas
        int limite = Math.min(recetasCalculadas.size(), 10);

        for (int i = 0; i < limite; i++) {
            RecetaCalculadaDTO receta = recetasCalculadas.get(i);

            Long idReceta = receta.getId();
            Log.d(TAG, "Id receta para tarjeta #" + i + ": " + idReceta);
            if (idReceta == null) {
                Log.e(TAG, "Id de receta es null en posición " + i);
                continue; // Saltar esta receta para evitar problemas
            }

            View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_receta, tarjetaContainer, false);

            TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
            TextView txtAutor = tarjeta.findViewById(R.id.nombreautor);
            TextView txtCalificacion = tarjeta.findViewById(R.id.calificacion);
            TextView txtPorciones = tarjeta.findViewById(R.id.txt_porciones);
            TextView txtTiempo = tarjeta.findViewById(R.id.tiempo);
            ImageView imagenReceta = tarjeta.findViewById(R.id.imagen_receta);
            ToggleButton btnFavorito = tarjeta.findViewById(R.id.btn_favorito);

            // Ocultar fecha y botón verde (como ya hacías)
            tarjeta.findViewById(R.id.txt_fecha).setVisibility(View.GONE);
            tarjeta.findViewById(R.id.botonVerde).setVisibility(View.GONE);

            // Setear datos
            txtNombre.setText(receta.getNombre());
            txtAutor.setText(receta.getAutorOriginal());
            txtTiempo.setText(String.valueOf(receta.getTiempoOriginal()) + "'");
            txtCalificacion.setText(receta.getCalificacionOriginal());
            txtPorciones.setText(String.valueOf(receta.getPorciones()));

            // Cargar imagen con Glide
            String fotoUrl = receta.getFotoOriginal();
            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(fotoUrl)
                        .centerCrop()
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imagenReceta);
            } else {
                imagenReceta.setImageResource(R.drawable.imagen_default);
            }

            btnFavorito.setVisibility(View.GONE);
            tarjeta.setTag(idReceta);

            // Opcional: Si querés mostrar ingredientes dentro de la tarjeta,
            // y el DTO tiene lista de ingredientes, acá podrías:
            // RecyclerView rvIngredientes = tarjeta.findViewById(R.id.rv_ingredientes);
            // Inicializar y setear adapter con receta.getIngredientes();
            // Y controlar visibilidad según si tiene o no ingredientes.

            tarjeta.setOnClickListener(v -> {
                if (modoSeleccion) {
                    View overlaySeleccion = v.findViewById(R.id.overlay_seleccion);
                    View seleccionada = v.findViewById(R.id.txt_seleccionada);
                    View iconoSeleccion = v.findViewById(R.id.icono_seleccion);

                    if (tarjetasSeleccionadas.contains(v)) {
                        tarjetasSeleccionadas.remove(v);
                        overlaySeleccion.setVisibility(View.GONE);
                        seleccionada.setVisibility(View.GONE);
                        iconoSeleccion.setVisibility(View.GONE);
                    } else {
                        tarjetasSeleccionadas.add(v);
                        overlaySeleccion.setVisibility(View.VISIBLE);
                        seleccionada.setVisibility(View.VISIBLE);
                        iconoSeleccion.setVisibility(View.VISIBLE);
                    }
                } else {
                    Long id = (Long) v.getTag();
                    if (id == null) {
                        Log.e(TAG, "Id de receta es null en tag");
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putLong("recetaCalculadaId", id);
                    Navigation.findNavController(v).navigate(
                            R.id.nav_recetas_calculadas_to_detalleRecetaCalculadaFragment,
                            bundle
                    );
                }
            });

            tarjetaContainer.addView(tarjeta);
        }
    }

    private void borrarRecetasSeleccionadas() {
        if (tarjetasSeleccionadas.isEmpty()) {
            Toast.makeText(getContext(), "No hay recetas seleccionadas para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = sharedPreferences.getLong("id_usuario", -1);
        if (usuarioId == -1) {
            Toast.makeText(getContext(), "ID de usuario no encontrado", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ID de usuario no encontrado en SharedPreferences al eliminar.");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        AtomicInteger eliminadas = new AtomicInteger(0);
        AtomicBoolean huboError = new AtomicBoolean(false);
        int total = tarjetasSeleccionadas.size();

        List<View> tarjetasParaEliminar = new ArrayList<>(tarjetasSeleccionadas);

        for (View tarjeta : tarjetasParaEliminar) {
            Object tag = tarjeta.getTag();
            if (!(tag instanceof Long)) {
                Log.w(TAG, "Tag de tarjeta no es Long: " + tag);
                huboError.set(true);
                if (eliminadas.incrementAndGet() == total) {
                    cargarDatosDeRecetasCalculadas();
                    Toast.makeText(getContext(), "Operación finalizada con errores", Toast.LENGTH_SHORT).show();
                    limpiarSeleccion();
                }
                continue;
            }
            Long idRecetaCalculada = (Long) tag;

            apiService.eliminarRecetaEscalada(idRecetaCalculada).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        tarjetaContainer.removeView(tarjeta);
                        tarjetasSeleccionadas.remove(tarjeta);
                        Log.d(TAG, "Receta eliminada con id: " + idRecetaCalculada);
                    } else {
                        Log.e(TAG, "Error al eliminar receta id " + idRecetaCalculada + ": " + response.code());
                        huboError.set(true);
                    }

                    if (eliminadas.incrementAndGet() == total) {
                        cargarDatosDeRecetasCalculadas();
                        Toast.makeText(getContext(), huboError.get() ? "Operación finalizada con errores" : "Recetas eliminadas", Toast.LENGTH_SHORT).show();
                        limpiarSeleccion();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Fallo al eliminar receta id " + idRecetaCalculada, t);
                    huboError.set(true);
                    if (eliminadas.incrementAndGet() == total) {
                        cargarDatosDeRecetasCalculadas();
                        Toast.makeText(getContext(), "Fallo al eliminar una o más recetas", Toast.LENGTH_SHORT).show();
                        limpiarSeleccion();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!filtroBusquedaActual.isEmpty()) {
            filtrarRecetas(filtroBusquedaActual);
        }
    }

    private void limpiarSeleccion() {
        for (View tarjeta : tarjetasSeleccionadas) {
            tarjeta.findViewById(R.id.overlay_seleccion).setVisibility(View.GONE);
            tarjeta.findViewById(R.id.txt_seleccionada).setVisibility(View.GONE);
            tarjeta.findViewById(R.id.icono_seleccion).setVisibility(View.GONE);
        }
        tarjetasSeleccionadas.clear();
    }
}
