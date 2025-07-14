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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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

import com.example.sapori.model.ComentarioValoracion;
import com.example.sapori.model.PasoReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRecetaFragment extends Fragment {

    private ViewPager2 viewPagerImagenes;
    private ImageView btnLapiz;
    private boolean desdeGestionar = false;
    private TextView txtTitulo, txtAutor, txtDescripcion, txtTiempo, txtPorciones, txtCalificacion;
    private RecyclerView rvIngredientes, rvComentarios;
    private LinearLayout layoutPasos;
    private LinearLayout layoutDots;

    private ImageButton btnFavorito;

    private ImageView flecha;

    private EditText editComentario;
    private RatingBar ratingBar;
    private TextView btnEnviarValoracion, btnEscalar;

    private Receta receta;
    private ApiService apiService;
    private ComentarioAdapter comentarioAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_receta, container, false);

        if (getArguments() != null) {
            desdeGestionar = getArguments().getBoolean("desdeGestionar", false);
        }



        // Vistas
        layoutDots = view.findViewById(R.id.indicatorLayout);
        viewPagerImagenes = view.findViewById(R.id.viewPagerImagenes);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtAutor = view.findViewById(R.id.txtAutor);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtTiempo = view.findViewById(R.id.txtTiempo);
        txtPorciones = view.findViewById(R.id.txtPorciones);
        txtCalificacion = view.findViewById(R.id.txtCalificacion);

        rvIngredientes = view.findViewById(R.id.recyclerIngredientes);
        layoutPasos = view.findViewById(R.id.layoutPasos);
        rvComentarios = view.findViewById(R.id.recyclerComentarios);

        editComentario = view.findViewById(R.id.editComentario);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnEnviarValoracion = view.findViewById(R.id.btnEnviarValoracion);
        btnEscalar = view.findViewById(R.id.btnEscalar);
        flecha = view.findViewById(R.id.btnAtras);
        btnFavorito = view.findViewById(R.id.btnFavorito);


        btnLapiz = view.findViewById(R.id.btnLapiz);
        btnLapiz.setOnClickListener(v -> {
            if (receta != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("receta", receta);
                // Navegación para editar receta (ejemplo)
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_detalleReceta_to_editarReceta, bundle);
            }
        });

        rvIngredientes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvComentarios.setLayoutManager(new LinearLayoutManager(getContext()));

        btnEnviarValoracion.setOnClickListener(v -> enviarComentario());

        btnEscalar.setOnClickListener(v -> {
            if (receta != null) {
                Bundle args = new Bundle();
                args.putSerializable("receta", receta);

                PopupEscalarFragment popup = new PopupEscalarFragment();
                popup.setArguments(args);
                popup.show(getParentFragmentManager(), "PopupEscalar");
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

        // Obtener ID desde argumentos
        Long recetaId = getArguments() != null ? getArguments().getLong("recetaId") : null;
        Log.d("DetalleReceta", "ID recibido: " + recetaId);

        if (recetaId != null) {
            apiService = ApiClient.getApiService();
            obtenerRecetaDesdeBackend(recetaId);
        }

        return view;
    }


    private void obtenerRecetaDesdeBackend(Long recetaId) {
        Call<Receta> call = apiService.obtenerReceta(recetaId);

        call.enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(@NonNull Call<Receta> call, @NonNull Response<Receta> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    receta = response.body();
                    cargarDatosEnVista();

                    // No seteamos visibilidad aquí
                    // Solo actualizar icono favorito
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<Receta> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión", t);
            }
        });
    }


    private void cargarDatosEnVista() {
        txtTitulo.setText(receta.getNombre());
        txtAutor.setText(receta.getAutor() != null ? receta.getAutor().getAlias() : "Autor desconocido");
        txtDescripcion.setText(receta.getDescripcion());
        txtTiempo.setText(receta.getTiempo() + " min");
        txtPorciones.setText(receta.getPorciones() + " porciones");
        txtCalificacion.setText(
                receta.getCalificacion() != null ? String.valueOf(receta.getCalificacion()) : "Sin calificar"
        );

        ImagenURLAdapter imagenAdapter = new ImagenURLAdapter(receta.getFotosPlato());
        viewPagerImagenes.setAdapter(imagenAdapter);

        IngredienteAdapter2 ingredienteAdapter2 = new IngredienteAdapter2(receta.getIngredientes());
        rvIngredientes.setAdapter(ingredienteAdapter2);

        layoutPasos.removeAllViews();

        Typeface fuentePersonalizada = ResourcesCompat.getFont(getContext(), R.font.poppinsmedium);

// Espacio arriba para que no quede pegado al borde superior
        View espacioArriba = new View(getContext());
        LinearLayout.LayoutParams espacioParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())
        );
        espacioArriba.setLayoutParams(espacioParams);
        layoutPasos.addView(espacioArriba);

// Altura de línea en dp convertida a píxeles (1.3dp)
        int alturaLinea = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics());

// Ancho línea en dp convertida a píxeles (200dp)
        int anchoLinea = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());

        for (PasoReceta paso : receta.getPasos()) {
            View pasoView = LayoutInflater.from(getContext()).inflate(R.layout.item_paso, layoutPasos, false);
            TextView numero = pasoView.findViewById(R.id.txtNumeroPaso);
            TextView descripcion = pasoView.findViewById(R.id.txtDescripcionPaso);

            numero.setText("Paso " + paso.getNumeroPaso());
            descripcion.setText(paso.getDescripcion());
            descripcion.setTypeface(fuentePersonalizada);
            descripcion.setTextColor(Color.parseColor("#9D9D9D"));

            layoutPasos.addView(pasoView);

            // Agregar línea divisora excepto después del último paso
            if (paso != receta.getPasos().get(receta.getPasos().size() - 1)) {
                View lineaDivisora = new View(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        anchoLinea,
                        alturaLinea
                );
                params.setMargins(0, 0, 0, 16);
                params.gravity = Gravity.CENTER_HORIZONTAL;  // Centra la línea horizontalmente
                lineaDivisora.setLayoutParams(params);
                lineaDivisora.setBackgroundColor(Color.parseColor("#D3D3D3")); // gris un poco más oscuro para mejor visibilidad
                layoutPasos.addView(lineaDivisora);
            }
        }

        List<ComentarioValoracion> comentarios = receta.getComentarios();
        if (comentarios != null) {
            comentarioAdapter = new ComentarioAdapter(comentarios);
            rvComentarios.setAdapter(comentarioAdapter);
        }

        calcularYMostrarCalificacion(comentarios, receta);
        comentarioAdapter.notifyDataSetChanged();

        agregarIndicadoresDots(receta.getFotosPlato().size(), 0);
        viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                agregarIndicadoresDots(receta.getFotosPlato().size(), position);
            }
        });

        // Mostrar o esconder el lápiz según desdeGestionar y autor
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = userPrefs.getLong("id_usuario", -1);

        if (desdeGestionar && receta.getAutor() != null && receta.getAutor().getId() == usuarioId) {
            btnLapiz.setVisibility(View.VISIBLE);
            btnEscalar.setVisibility(View.GONE);
            btnFavorito.setVisibility(View.GONE);
        } else {
            btnLapiz.setVisibility(View.GONE);
            btnFavorito.setVisibility(View.VISIBLE);
            btnEscalar.setVisibility(View.VISIBLE);
        }
    }

    private void calcularYMostrarCalificacion(List<ComentarioValoracion> comentarios, Receta receta) {
        if (comentarios == null || comentarios.isEmpty()) {
            receta.setCalificacion(null);
            txtCalificacion.setText("Sin calificar");
            return;
        }

        double suma = 0;
        int cantidad = 0;

        for (ComentarioValoracion comentario : comentarios) {
            if (comentario.getPuntaje() != null) {
                suma += comentario.getPuntaje();
                cantidad++;
            }
        }

        if (cantidad > 0) {
            double promedio = suma / cantidad;
            receta.setCalificacion(promedio);

            txtCalificacion.setText(String.format(Locale.getDefault(), "%.1f", promedio));
        } else {
            receta.setCalificacion(null);
            txtCalificacion.setText("Sin calificar");
        }
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


    private void enviarComentario() {
        String texto = editComentario.getText().toString().trim();
        int puntuacion = Math.round(ratingBar.getRating());

        if (texto.isEmpty()) {
            editComentario.setError("Escribe un comentario");
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("id_usuario", -1);
        Log.d("DetalleReceta", "userId recuperado de SharedPreferences: " + userId);

        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            Log.e("DetalleReceta", "Usuario no identificado, userId = -1");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setId(userId);

        ComentarioValoracion nuevoComentario = new ComentarioValoracion();
        nuevoComentario.setTextoComentario(texto);
        nuevoComentario.setPuntaje(puntuacion);
        nuevoComentario.setUsuario(usuario);

        Log.d("DetalleReceta", "Enviando comentario con usuario id: " + userId + ", texto: " + texto + ", puntaje: " + puntuacion);

        apiService.enviarComentario(receta.getId(), nuevoComentario).enqueue(new Callback<ComentarioValoracion>() {
            @Override
            public void onResponse(@NonNull Call<ComentarioValoracion> call, @NonNull Response<ComentarioValoracion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ComentarioValoracion comentarioCreado = response.body();
                    receta.getComentarios().add(0, comentarioCreado);
                    comentarioAdapter.notifyItemInserted(0);
                    rvComentarios.scrollToPosition(0);

                    // Limpiar inputs
                    editComentario.setText("");
                    ratingBar.setRating(0);

                    Toast.makeText(getContext(), "Comentario enviado", Toast.LENGTH_SHORT).show();

                    // Volver a consultar la receta actualizada con la calificación correcta
                    apiService.obtenerReceta(receta.getId()).enqueue(new Callback<Receta>() {
                        @Override
                        public void onResponse(Call<Receta> call, Response<Receta> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                receta = response.body();
                                // Actualizar la calificación en UI
                                txtCalificacion.setText(
                                        receta.getCalificacion() != null
                                                ? String.format(Locale.getDefault(), "%.1f", receta.getCalificacion())
                                                : "Sin calificar"
                                );

                                // Avisar para recargar las tarjetas con la nueva calificación
                                Bundle result = new Bundle();
                                result.putBoolean("recargar_recetas", true);
                                getParentFragmentManager().setFragmentResult("actualizar_calificacion", result);
                            }
                        }

                        @Override
                        public void onFailure(Call<Receta> call, Throwable t) {
                            Log.e("DetalleReceta", "Error al obtener receta actualizada", t);
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Error al enviar comentario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComentarioValoracion> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Fallo de red al enviar comentario", Toast.LENGTH_SHORT).show();
                Log.e("DetalleReceta", "Error al hacer request de comentario", t);
            }
        });
    }
}
