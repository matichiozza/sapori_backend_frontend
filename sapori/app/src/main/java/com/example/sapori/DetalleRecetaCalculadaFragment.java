package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sapori.model.IngredienteDTO;
import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRecetaCalculadaFragment extends Fragment {

    private RecetaCalculadaDTO receta;  // <-- variable para guardar la receta obtenida

    private LinearLayout layoutDots;
    private ViewPager2 viewPagerImagenes;
    private TextView txtTitulo, txtAutor, txtDescripcion, txtTiempo, txtPorciones, txtCalificacion;
    private RecyclerView rvIngredientes;
    private LinearLayout layoutPasos;
    private ImageButton flecha;
    private ImageView btnFavorito;
    private ApiService apiService;
    private boolean desdeGestionar;
    private TextView btnDetalleAjuste;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_receta_calculada, container, false);

        // Inicializar vistas primero
        btnDetalleAjuste = view.findViewById(R.id.btnDetalleAjuste);
        layoutDots = view.findViewById(R.id.indicatorLayout);
        viewPagerImagenes = view.findViewById(R.id.viewPagerImagenes);
        txtTitulo = view.findViewById(R.id.txtTituloCalculado);
        txtAutor = view.findViewById(R.id.txtAutor);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtTiempo = view.findViewById(R.id.txtTiempo);
        txtPorciones = view.findViewById(R.id.txtPorciones);
        txtCalificacion = view.findViewById(R.id.txtCalificacion);
        rvIngredientes = view.findViewById(R.id.recyclerIngredientesCalculados);
        layoutPasos = view.findViewById(R.id.layoutPasosCalculados);
        flecha = view.findViewById(R.id.btnAtras);

        rvIngredientes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Obtener argumentos
        long recetaId = getArguments() != null ? getArguments().getLong("recetaCalculadaId", -1L) : -1L;
        desdeGestionar = getArguments() != null && getArguments().getBoolean("desdeGestionar", false);

        Log.d("DetalleRecetaCalculada", "ID recibido: " + recetaId);

        if (recetaId != -1L) {
            apiService = ApiClient.getApiService();
            obtenerRecetaDesdeBackend(recetaId);  // Aquí cambio la llamada para obtener toda la receta
        } else {
            Toast.makeText(requireContext(), "ID de receta inválido o no recibido", Toast.LENGTH_SHORT).show();
        }

        btnDetalleAjuste.setOnClickListener(v -> {
            if (receta != null) {
                Bundle args = new Bundle();
                args.putSerializable("receta", receta);  // Asegurate que RecetaCalculadaDTO implemente Serializable

                PopupRecetaEscaladaFragment popup = new PopupRecetaEscaladaFragment();
                popup.setArguments(args);
                popup.show(getParentFragmentManager(), "PopupRecetaEscaladaFragment");
            } else {
                Toast.makeText(getContext(), "Receta no disponible", Toast.LENGTH_SHORT).show();
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

    private void obtenerRecetaDesdeBackend(Long recetaId) {
        Call<RecetaCalculadaDTO> call = apiService.obtenerRecetaCalculadaPorId(recetaId);

        call.enqueue(new Callback<RecetaCalculadaDTO>() {
            @Override
            public void onResponse(@NonNull Call<RecetaCalculadaDTO> call, @NonNull Response<RecetaCalculadaDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    receta = response.body();

                    Log.d("API", "RecetaCalculada obtenida: " + receta.getNombre() + " (ID: " + receta.getId() + ")");


                    cargarDatosEnVista(receta);

                    // Llamo a la carga específica de ingredientes luego de cargar la receta completa
                    obtenerIngredientesDesdeBackend(recetaId);

                    SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                    long usuarioId = userPrefs.getLong("id_usuario", -1);

                    if (usuarioId == -1) {
                        Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
                        actualizarIconoFavorito(false);
                        return;
                    }
                    obtenerFavoritosYActualizarIcono(usuarioId);

                } else {
                    Log.e("API", "Error en respuesta: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar receta calculada: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RecetaCalculadaDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión", t);
                Toast.makeText(requireContext(), "Error de conexión al cargar receta calculada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerIngredientesDesdeBackend(Long recetaId) {
        Call<List<IngredienteDTO>> call = apiService.getIngredientesJsonPorReceta(recetaId);

        call.enqueue(new Callback<List<IngredienteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<IngredienteDTO>> call, @NonNull Response<List<IngredienteDTO>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<IngredienteDTO> ingredientes = response.body();

                    Log.d("API", "Ingredientes obtenidos: " + ingredientes.size());

                    if (receta != null) {
                        receta.setIngredientes(ingredientes);

                        IngredientesAdapter ingredientesAdapter = new IngredientesAdapter(ingredientes);
                        rvIngredientes.setAdapter(ingredientesAdapter);
                    }

                } else {
                    Log.e("API", "Error en respuesta ingredientes: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar ingredientes: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<IngredienteDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión ingredientes", t);
                Toast.makeText(requireContext(), "Error de conexión al cargar ingredientes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosEnVista(RecetaCalculadaDTO receta) {
        if (receta == null) return;

        txtTitulo.setText(receta.getNombre());
        txtAutor.setText(receta.getAutorOriginal() != null ? receta.getAutorOriginal() : "Autor desconocido");
        txtDescripcion.setText(receta.getDescripcionOriginal());
        txtTiempo.setText(receta.getTiempoOriginal() != null ? receta.getTiempoOriginal() + " min" : "Tiempo no disponible");
        txtPorciones.setText(receta.getPorciones() != null ? receta.getPorciones() + " porciones" : "Porciones no disponibles");
        txtCalificacion.setText(
                receta.getCalificacionOriginal() != null && !receta.getCalificacionOriginal().isEmpty()
                        ? receta.getCalificacionOriginal()
                        : "Sin calificar"
        );

        // Fotos
        List<String> fotos = receta.getFotoOriginal() != null ? List.of(receta.getFotoOriginal()) : List.of();
        ImagenURLAdapter imagenAdapter = new ImagenURLAdapter(fotos);
        viewPagerImagenes.setAdapter(imagenAdapter);

        // Ingredientes (aca puede estar vacio, luego se actualizará con la llamada de ingredientes)
        if (receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()) {
            IngredientesAdapter ingredientesAdapter = new IngredientesAdapter(receta.getIngredientes());
            rvIngredientes.setAdapter(ingredientesAdapter);
        }

        // Pasos
        layoutPasos.removeAllViews();
        Typeface fuentePersonalizada = ResourcesCompat.getFont(getContext(), R.font.poppinsmedium);

        View espacioArriba = new View(getContext());
        LinearLayout.LayoutParams espacioParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())
        );
        espacioArriba.setLayoutParams(espacioParams);
        layoutPasos.addView(espacioArriba);

        int alturaLinea = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics());
        int anchoLinea = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());

        for (int i = 0; i < receta.getPasos().size(); i++) {
            String pasoDescripcion = receta.getPasos().get(i);

            View pasoView = LayoutInflater.from(getContext()).inflate(R.layout.item_paso, layoutPasos, false);
            TextView numero = pasoView.findViewById(R.id.txtNumeroPaso);
            TextView descripcion = pasoView.findViewById(R.id.txtDescripcionPaso);

            numero.setText("Paso " + (i + 1));
            descripcion.setText(pasoDescripcion);
            descripcion.setTypeface(fuentePersonalizada);
            descripcion.setTextColor(Color.parseColor("#9D9D9D"));

            layoutPasos.addView(pasoView);

            if (i != receta.getPasos().size() - 1) {
                View lineaDivisora = new View(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(anchoLinea, alturaLinea);
                params.setMargins(0, 0, 0, 16);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                lineaDivisora.setLayoutParams(params);
                lineaDivisora.setBackgroundColor(Color.parseColor("#D3D3D3"));
                layoutPasos.addView(lineaDivisora);
            }
        }

        // Dots indicadores para ViewPager
        agregarIndicadoresDots(fotos.size(), 0);
        viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                agregarIndicadoresDots(fotos.size(), position);
            }
        });

        // Favorito visible
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = userPrefs.getLong("id_usuario", -1);
    }

    private void obtenerFavoritosYActualizarIcono(long usuarioId) {
        Call<List<Receta>> callFavoritos = apiService.obtenerRecetasFavoritasPorId(usuarioId);
        callFavoritos.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Set<Long> idsFavoritos = new HashSet<>();
                    for (Receta r : response.body()) {
                        idsFavoritos.add(r.getId());
                    }
                    boolean esFavorita = receta != null && idsFavoritos.contains(receta.getId());
                    actualizarIconoFavorito(esFavorita);
                } else {
                    Log.e("FavoritosAPI", "No se pudieron obtener favoritos. Código: " + response.code());
                    actualizarIconoFavorito(false);
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("FavoritosAPI", "Error al obtener recetas favoritas", t);
                actualizarIconoFavorito(false);
            }
        });
    }

    private void actualizarIconoFavorito(boolean esFavorita) {
        if (btnFavorito == null) return;

        if (esFavorita) {
            btnFavorito.setImageResource(R.drawable.corazon_relleno);
        } else {
            btnFavorito.setImageResource(R.drawable.corazon_vacio);
        }
    }

    private void agregarIndicadoresDots(int total, int seleccionado) {
        layoutDots.removeAllViews();
        for (int i = 0; i < total; i++) {
            ImageView dot = new ImageView(getContext());
            dot.setImageResource(i == seleccionado ? R.drawable.dot_selected : R.drawable.dot_unselected);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            layoutDots.addView(dot, params);
        }
    }
}
