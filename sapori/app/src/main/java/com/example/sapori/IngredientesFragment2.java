package com.example.sapori;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class IngredientesFragment2 extends DialogFragment {

    private static final String TAG = "IngredientesFragment2";
    private LinearLayout listaIngredientes2;
    private Set<String> ingredientesSeleccionados = new HashSet<>();

    // Listener para notificar cuando se guarden ingredientes
    private Runnable onGuardarListener;

    public IngredientesFragment2() {
        // Constructor vacío requerido
    }

    public void setOnGuardarListener(Runnable listener) {
        this.onGuardarListener = listener;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflando layout fragment_ingredientes2");
        View view = inflater.inflate(R.layout.fragment_ingredientes2, container, false);

        listaIngredientes2 = view.findViewById(R.id.lista_ingredientes2);

        View popupLayout2 = view.findViewById(R.id.popup_layout2);
        TextView preparacion = view.findViewById(R.id.preparacion);
        TextView textingredientes2 = view.findViewById(R.id.text_ingredientes2);
        EditText preparacion2 = view.findViewById(R.id.edit_preparacion);

        SharedPreferences prefs = requireContext().getSharedPreferences("ingredientes_prefs2", Context.MODE_PRIVATE);
        ingredientesSeleccionados = new HashSet<>(prefs.getStringSet("ingredientes_seleccionados2", new HashSet<>()));

        Log.d(TAG, "Ingredientes seleccionados cargados: " + ingredientesSeleccionados);

        Button btnGuardarIngredientes = view.findViewById(R.id.btn_guardar_ingredientes2);
        if (btnGuardarIngredientes != null) {
            btnGuardarIngredientes.setOnClickListener(v -> {
                guardarIngredientesSeleccionados();

                // Enviar resultado con los IDs de ingredientes seleccionados
                Bundle result = new Bundle();
                // Convertimos Set<String> a ArrayList<String> para enviar en Bundle
                ArrayList<String> idsSeleccionados = new ArrayList<>(ingredientesSeleccionados);
                result.putStringArrayList("ingredientesSeleccionados", idsSeleccionados);
                getParentFragmentManager().setFragmentResult("ingredientesSeleccionadosKey", result);

                if (onGuardarListener != null) {
                    onGuardarListener.run();
                }
                dismiss();
            });
        } else {
            Log.e(TAG, "No se encontró el botón guardar ingredientes en el layout");
        }

        Log.d(TAG, "onCreateView: Iniciando carga de ingredientes");
        cargarIngredientes();

        popupLayout2.setVisibility(View.VISIBLE);
        preparacion.setVisibility(View.GONE);
        preparacion2.setVisibility(View.GONE);
        textingredientes2.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            View decorView = getDialog().getWindow().getDecorView();
            decorView.setPadding(0, 0, 0, 0);
        }
    }

    private void guardarIngredientesSeleccionados() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ingredientes_prefs2", Context.MODE_PRIVATE);
        prefs.edit().putStringSet("ingredientes_seleccionados2", ingredientesSeleccionados).apply();
        Log.d(TAG, "Ingredientes seleccionados guardados: " + ingredientesSeleccionados);
    }

    private void cargarIngredientes() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Ingrediente>> call = apiService.listarIngredientes();

        call.enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Carga de ingredientes exitosa. Cantidad: " + response.body().size());
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
        listaIngredientes2.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Log.d(TAG, "Mostrando ingredientes. Ingredientes seleccionados: " + ingredientesSeleccionados);

        for (Ingrediente ingrediente : ingredientes) {
            Log.d(TAG, "Ingrediente: " + ingrediente.getNombre() + " (ID: " + ingrediente.getId() + ")");

            View itemView = inflater.inflate(R.layout.item_ingrediente2, listaIngredientes2, false);

            TextView nombre = itemView.findViewById(R.id.nombre_ingrediente);
            ImageView imagen = itemView.findViewById(R.id.imagen_ingrediente);
            CheckBox checkBox = itemView.findViewById(R.id.checkbox_ingrediente);

            if (nombre != null) {
                nombre.setText(ingrediente.getNombre());
            }

            if (checkBox != null) {
                boolean seleccionado = ingredientesSeleccionados.contains(String.valueOf(ingrediente.getId()));
                Log.d(TAG, "Checkbox para ingrediente " + ingrediente.getNombre() + " seleccionado: " + seleccionado);
                checkBox.setChecked(seleccionado);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        ingredientesSeleccionados.add(String.valueOf(ingrediente.getId()));
                        Log.d(TAG, "Ingrediente agregado: " + ingrediente.getNombre());
                    } else {
                        ingredientesSeleccionados.remove(String.valueOf(ingrediente.getId()));
                        Log.d(TAG, "Ingrediente removido: " + ingrediente.getNombre());
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

            listaIngredientes2.addView(itemView);
        }
    }

    // Supongamos que tienes un método que se llama al seleccionar un ingrediente:
    private void onIngredienteSeleccionado(Ingrediente ingrediente) {
        Bundle result = new Bundle();
        result.putSerializable("ingrediente_seleccionado", ingrediente);
        getParentFragmentManager().setFragmentResult("seleccion_ingrediente", result);
        dismiss();
    }
}
