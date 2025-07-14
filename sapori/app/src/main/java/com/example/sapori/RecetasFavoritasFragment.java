package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecetasFavoritasFragment extends Fragment {

    private EditText editBusqueda;
    private LinearLayout tarjetaContainer;
    private List<Receta> recetasFavoritasOriginales = new ArrayList<>();
    private ImageView flecha;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recetas_favoritas, container, false);

        tarjetaContainer = view.findViewById(R.id.tarjeta_container);
        flecha = view.findViewById(R.id.flecha);
        editBusqueda = view.findViewById(R.id.edit_busqueda);

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Listener para filtrar recetas al escribir
        editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filtrarRecetasFavoritas(s.toString());
            }
        });

        cargarRecetasFavoritasDesdeApi();

        return view;
    }

    private void cargarRecetasFavoritasDesdeApi() {
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long userId = userPrefs.getLong("id_usuario", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<Receta>> call = apiService.obtenerRecetasFavoritasPorId(userId);

        call.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recetasFavoritasOriginales.clear();
                    recetasFavoritasOriginales.addAll(response.body());
                    mostrarFavoritos(recetasFavoritasOriginales);
                } else {
                    Toast.makeText(requireContext(), "No tiene recetas favoritas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarFavoritos(List<Receta> recetasFavoritas) {
        new Handler(Looper.getMainLooper()).post(() -> {
            tarjetaContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (recetasFavoritas.isEmpty()) {
                Toast.makeText(getContext(), "No se encontraron recetas favoritas", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Receta receta : recetasFavoritas) {
                View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_receta, tarjetaContainer, false);

                TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
                TextView txtAutor = tarjeta.findViewById(R.id.nombreautor);
                TextView txtCalificacion = tarjeta.findViewById(R.id.calificacion);
                TextView txtPorciones = tarjeta.findViewById(R.id.txt_porciones);
                TextView txtTiempo = tarjeta.findViewById(R.id.tiempo);
                TextView txtFecha = tarjeta.findViewById(R.id.txt_fecha);
                ImageView imagenReceta = tarjeta.findViewById(R.id.imagen_receta);
                ToggleButton btnFavorito = tarjeta.findViewById(R.id.btn_favorito);

                String nombre = receta.getNombre() != null ? receta.getNombre() : "Sin nombre";
                String autor = (receta.getAutor() != null && receta.getAutor().getNombre() != null)
                        ? receta.getAutor().getNombre() : "Autor desconocido";
                String calificacion = receta.getCalificacion() != null
                        ? String.format("%.1f", receta.getCalificacion()) : "N/D";
                int porciones = receta.getPorciones() != null ? receta.getPorciones() : 0;
                int tiempo = receta.getTiempo() != null ? receta.getTiempo() : 0;
                String fecha = receta.getFechaCreacion() != null ? receta.getFechaCreacion().split("T")[0] : "Fecha no disponible";
                String fotoPrincipal = (receta.getFotosPlato() != null && !receta.getFotosPlato().isEmpty())
                        ? receta.getFotosPlato().get(0) : "";
                String idReceta = receta.getId() != null ? receta.getId().toString() : "";

                txtNombre.setText(nombre);
                txtAutor.setText(autor);
                txtCalificacion.setText(calificacion);
                txtPorciones.setText(String.valueOf(porciones));
                txtTiempo.setText(tiempo + "'");
                txtFecha.setText(fecha);

                if (!fotoPrincipal.isEmpty()) {
                    Glide.with(requireContext())
                            .load(fotoPrincipal)
                            .centerCrop()
                            .placeholder(R.drawable.imagen_default)
                            .into(imagenReceta);
                } else {
                    imagenReceta.setImageResource(R.drawable.imagen_default);
                }

                btnFavorito.setOnCheckedChangeListener(null);
                btnFavorito.setChecked(true);

                btnFavorito.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!isChecked) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("FavoritosPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        if (!idReceta.isEmpty()) {
                            editor.remove(idReceta);
                            editor.apply();
                            tarjetaContainer.removeView(tarjeta);
                            Toast.makeText(getContext(), nombre + " quitada de favoritos", Toast.LENGTH_SHORT).show();

                            // Aviso al otro fragment que se eliminó este favorito
                            Bundle result = new Bundle();
                            try {
                                long recetaIdLong = Long.parseLong(idReceta);
                                result.putLong("recetaIdEliminada", recetaIdLong);
                                getParentFragmentManager().setFragmentResult("keyFavoritoEliminado", result);
                            } catch (NumberFormatException e) {
                                Log.e("FavoritosAPI", "Error parseando idReceta para setFragmentResult: " + idReceta, e);
                            }

                            // Llamada API para eliminar favorito en la base de datos
                            SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                            long userId = userPrefs.getLong("id_usuario", -1);
                            if (userId != -1) {
                                try {
                                    long recetaIdLong = Long.parseLong(idReceta);
                                    ApiService apiService = ApiClient.getApiService();
                                    Call<Void> deleteCall = apiService.eliminarRecetaDeFavoritos(userId, recetaIdLong);

                                    deleteCall.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Log.d("FavoritosAPI", "✅ Receta eliminada de favoritos en la BDD correctamente");
                                            } else {
                                                Log.e("FavoritosAPI", "❌ Fallo al eliminar de favoritos en la BDD. Código: " + response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e("FavoritosAPI", "❌ Error al llamar a DELETE favoritos", t);
                                        }
                                    });

                                } catch (NumberFormatException e) {
                                    Log.e("FavoritosAPI", "❌ Error al parsear idReceta para eliminar: " + idReceta, e);
                                }
                            }
                        }
                    }
                });

                if (receta.getId() != null) {
                    long recetaId = receta.getId();
                    tarjeta.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putLong("recetaId", recetaId);
                        Navigation.findNavController(v).navigate(R.id.action_nav_recetas_favoritas_to_detalleRecetaFragment, bundle);
                    });
                }

                tarjetaContainer.addView(tarjeta);
            }
        });
    }

    private void filtrarRecetasFavoritas(String texto) {
        List<Receta> filtradas = new ArrayList<>();
        for (Receta receta : recetasFavoritasOriginales) {
            if (receta.getNombre() != null && receta.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                filtradas.add(receta);
            }
        }

        mostrarFavoritos(filtradas);
    }
}
