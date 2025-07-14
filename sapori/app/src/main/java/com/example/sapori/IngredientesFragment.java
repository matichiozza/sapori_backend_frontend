package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IngredientesFragment extends DialogFragment {

    private static final String TAG = "IngredientesFragment";
    private LinearLayout listaIngredientes;
    private Set<String> ingredientesSeleccionados = new HashSet<>();

    public IngredientesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflando layout");
        View view = inflater.inflate(R.layout.fragment_ingredientes, container, false);

        listaIngredientes = view.findViewById(R.id.lista_ingredientes);

        View popupLayout = view.findViewById(R.id.popup_layout);
        TextView textQuitarIngrediente = view.findViewById(R.id.text_quitar_ingrediente);
        TextView textAñadirIngrediente = view.findViewById(R.id.text_por_ingrediente);
        View btnQuitarIngrediente = view.findViewById(R.id.btn_Quitar_ingrediente);
        View btnAñadirIngrediente = view.findViewById(R.id.btn_añadir_ingrediente);
        View btnLimpiarFiltros = view.findViewById(R.id.btn_limpiar_filtro);
        RelativeLayout layoutFiltros = view.findViewById(R.id.layout_filtros);
        ScrollView scrollFiltros = view.findViewById(R.id.scroll_filtros);

        TextView textOrdenarPor = view.findViewById(R.id.text_ordenar_por);
        HorizontalScrollView scrollOrdenarPor = null;
        if (textOrdenarPor != null) {
            scrollOrdenarPor = (HorizontalScrollView) textOrdenarPor.getRootView()
                    .findViewById(R.id.container_ordenamiento).getParent();
        }

        if (textOrdenarPor != null) textOrdenarPor.setVisibility(View.GONE);
        if (scrollOrdenarPor != null) scrollOrdenarPor.setVisibility(View.GONE);

        popupLayout.setVisibility(View.VISIBLE);
        btnQuitarIngrediente.setVisibility(View.GONE);
        btnLimpiarFiltros.setVisibility(View.GONE);
        textQuitarIngrediente.setVisibility(View.GONE);
        textAñadirIngrediente.setVisibility(View.GONE);
        btnAñadirIngrediente.setVisibility(View.GONE);
        layoutFiltros.setVisibility(View.GONE);
        scrollFiltros.setVisibility(View.GONE);

        SharedPreferences prefs = requireContext().getSharedPreferences("ingredientes_prefs", Context.MODE_PRIVATE);
        ingredientesSeleccionados = new HashSet<>(prefs.getStringSet("ingredientes_seleccionados", new HashSet<>()));

        Button btnGuardarIngredientes = view.findViewById(R.id.btn_guardar_ingredientes);
        if (btnGuardarIngredientes != null) {
            btnGuardarIngredientes.setOnClickListener(v -> {
                guardarIngredientesSeleccionados();
                dismiss();
            });
        } else {
            Log.e(TAG, "No se encontró el botón guardar ingredientes en el layout");
        }

        Log.d(TAG, "onCreateView: Iniciando carga de ingredientes");
        cargarIngredientes();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Quitar el fondo detrás del DialogFragment (fondo transparente)
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Quitar padding para que el layout ocupe todo el espacio posible sin bordes
            View decorView = getDialog().getWindow().getDecorView();
            decorView.setPadding(0, 0, 0, 0);
        }
    }

    private void guardarIngredientesSeleccionados() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ingredientes_prefs", Context.MODE_PRIVATE);
        prefs.edit().putStringSet("ingredientes_seleccionados", ingredientesSeleccionados).apply();
        Log.d(TAG, "Ingredientes seleccionados guardados: " + ingredientesSeleccionados);
    }

    private void cargarIngredientes() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Ingrediente>> call = apiService.listarIngredientes();

        call.enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarIngredientes(response.body());
                } else {
                    Log.e(TAG, "Respuesta no exitosa o body null. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                Log.e(TAG, "Error en llamada API: " + t.getMessage());
            }
        });
    }

    private void mostrarIngredientes(List<Ingrediente> ingredientes) {
        listaIngredientes.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Ingrediente ingrediente : ingredientes) {
            View itemView = inflater.inflate(R.layout.item_ingrediente, listaIngredientes, false);

            TextView nombre = itemView.findViewById(R.id.nombre_ingrediente);
            ImageView imagen = itemView.findViewById(R.id.imagen_ingrediente);
            CheckBox checkBox = itemView.findViewById(R.id.checkbox_ingrediente);

            if (nombre != null) {
                nombre.setText(ingrediente.getNombre());
            }

            if (checkBox != null) {
                checkBox.setChecked(ingredientesSeleccionados.contains(String.valueOf(ingrediente.getId())));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        ingredientesSeleccionados.add(String.valueOf(ingrediente.getId()));
                    } else {
                        ingredientesSeleccionados.remove(String.valueOf(ingrediente.getId()));
                    }
                });
            }

            if (imagen != null) {
                if (ingrediente.getImagenUrl() != null && !ingrediente.getImagenUrl().isEmpty()) {
                    Glide.with(imagen.getContext())
                            .load(ingrediente.getImagenUrl())
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error_image)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target, boolean isFirstResource) {
                                    Log.e(TAG, "Glide error: " + (e != null ? e.getMessage() : "unknown error"), e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target, DataSource dataSource,
                                                               boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(imagen);
                } else {
                    imagen.setImageResource(R.drawable.placeholder);
                }
            }

            listaIngredientes.addView(itemView);
        }
    }
}