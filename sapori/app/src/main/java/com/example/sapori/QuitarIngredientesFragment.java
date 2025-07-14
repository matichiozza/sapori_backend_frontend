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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuitarIngredientesFragment extends DialogFragment {

    private static final String TAG = "QuitarIngredientesFragment";
    private static final String PREFS_NAME = "ingredientes_prefs";
    private static final String KEY_INGREDIENTES_QUITAR = "ingredientes_seleccionados_a_quitar";

    private LinearLayout listaIngredientes1;
    private Set<String> ingredientesSeleccionados1 = new HashSet<>();

    public QuitarIngredientesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredientes3, container, false);

        listaIngredientes1 = view.findViewById(R.id.lista_ingredientes3);

        // Ocultar cosas innecesarias
        view.findViewById(R.id.popup_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btn_Quitar_ingrediente).setVisibility(View.GONE);
        view.findViewById(R.id.btn_añadir_ingrediente).setVisibility(View.GONE);
        view.findViewById(R.id.btn_limpiar_filtro).setVisibility(View.GONE);
        view.findViewById(R.id.text_quitar_ingrediente).setVisibility(View.GONE);
        view.findViewById(R.id.text_por_ingrediente).setVisibility(View.GONE);
        view.findViewById(R.id.layout_filtros).setVisibility(View.GONE);
        view.findViewById(R.id.scroll_filtros).setVisibility(View.GONE);

        TextView textOrdenarPor = view.findViewById(R.id.text_ordenar_por);
        if (textOrdenarPor != null) textOrdenarPor.setVisibility(View.GONE);

        HorizontalScrollView scrollOrdenarPor = null;
        if (textOrdenarPor != null) {
            scrollOrdenarPor = (HorizontalScrollView) textOrdenarPor.getRootView()
                    .findViewById(R.id.container_ordenamiento).getParent();
        }
        if (scrollOrdenarPor != null) scrollOrdenarPor.setVisibility(View.GONE);

        // Cargar selección previa
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ingredientesSeleccionados1.clear();
        ingredientesSeleccionados1.addAll(prefs.getStringSet(KEY_INGREDIENTES_QUITAR, new HashSet<>()));

        Button btnGuardar = view.findViewById(R.id.btn_guardar_ingredientes);
        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet(KEY_INGREDIENTES_QUITAR, new HashSet<>(ingredientesSeleccionados1));
                editor.apply();

                devolverIngredientesSeleccionados();
                dismiss();
            });
        }

        cargarIngredientes();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
    }

    private void devolverIngredientesSeleccionados() {
        Bundle result = new Bundle();
        result.putStringArrayList("ingredientes_seleccionados", new ArrayList<>(ingredientesSeleccionados1));
        getParentFragmentManager().setFragmentResult("ingredientesKey", result);
        Log.d(TAG, "Ingredientes seleccionados devueltos: " + ingredientesSeleccionados1);
    }

    private void cargarIngredientes() {
        ApiService apiService = ApiClient.getApiService();
        apiService.listarIngredientes().enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarIngredientes(response.body());
                } else {
                    Log.e(TAG, "Error: código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                Log.e(TAG, "Error en API: " + t.getMessage());
            }
        });
    }

    private void mostrarIngredientes(List<Ingrediente> ingredientes) {
        listaIngredientes1.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Ingrediente ingrediente : ingredientes) {
            View itemView = inflater.inflate(R.layout.item_ingrediente2, listaIngredientes1, false);
            TextView nombre = itemView.findViewById(R.id.nombre_ingrediente);
            ImageView imagen = itemView.findViewById(R.id.imagen_ingrediente);
            CheckBox checkBox = itemView.findViewById(R.id.checkbox_ingrediente);

            nombre.setText(ingrediente.getNombre());
            checkBox.setChecked(ingredientesSeleccionados1.contains(String.valueOf(ingrediente.getId())));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String idStr = String.valueOf(ingrediente.getId());
                if (isChecked) ingredientesSeleccionados1.add(idStr);
                else ingredientesSeleccionados1.remove(idStr);
            });

            if (ingrediente.getImagenUrl() != null && !ingrediente.getImagenUrl().isEmpty()) {
                Glide.with(imagen.getContext())
                        .load(ingrediente.getImagenUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(imagen);
            } else {
                imagen.setImageResource(R.drawable.placeholder);
            }

            listaIngredientes1.addView(itemView);
        }
    }
}
