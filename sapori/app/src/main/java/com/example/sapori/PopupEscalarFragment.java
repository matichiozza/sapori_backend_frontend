package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopupEscalarFragment extends DialogFragment {

    private TabLayout tabEscalado;
    private LinearLayout layoutPorPorciones;
    private LinearLayout layoutPorIngredientes;
    private EditText editTextPorciones;

    private boolean esPorIngredientes = false; // por defecto es por porciones

    private Spinner spinnerUnidadIngrediente;
    private int tipoCalculo = 0; // 0 = por porciones, 1 = por ingredientes

    private FrameLayout layoutSpinnerUnidad;
    private ImageView spinnerUnidadIcon;

    private RecetasViewModel recetasViewModel;

    private RecyclerView recyclerIngredientes1;
    private IngredienteAdapter3 ingredienteAdapter3;

    private View btnGuardarVersion;
    private List<IngredienteReceta> listaDeIngredienteRecetas;

    private Receta receta;


    public PopupEscalarFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PopupDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int width = 950;
            int height = 1600;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_escalar_receta, container, false);

        // Referencias UI
        tabEscalado = view.findViewById(R.id.tabEscalado);
        layoutPorPorciones = view.findViewById(R.id.layoutPorPorciones);
        layoutPorIngredientes = view.findViewById(R.id.layoutPorIngredientes);
        editTextPorciones = view.findViewById(R.id.editTextPorciones);
        recyclerIngredientes1 = view.findViewById(R.id.recyclerIngredientes1);
        btnGuardarVersion = view.findViewById(R.id.btnGuardarVersion);
        spinnerUnidadIngrediente = view.findViewById(R.id.spinnerUnidadIngrediente);
        layoutSpinnerUnidad = view.findViewById(R.id.layoutSpinnerUnidad);
        spinnerUnidadIcon = view.findViewById(R.id.spinnerUnidadIcon);

        // Referencias para botones y edición de cantidad
        LinearLayout layoutCantidadBotones = view.findViewById(R.id.layoutCantidadBotones);
        ImageView btnMas = view.findViewById(R.id.btnMas);
        ImageView btnMenos = view.findViewById(R.id.btnMenos);
        EditText editCantidad = view.findViewById(R.id.editCantidad);
        TextView txtUnidad = view.findViewById(R.id.txtUnidad);

        recetasViewModel = new ViewModelProvider(requireActivity()).get(RecetasViewModel.class);

        recyclerIngredientes1.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        tabEscalado.addTab(tabEscalado.newTab().setCustomView(createTabView("Por porciones")));
        tabEscalado.addTab(tabEscalado.newTab().setCustomView(createTabView("Por ingredientes")));

        Bundle args = getArguments();
        if (args != null) {
            receta = (Receta) args.getSerializable("receta");
        }

        if (receta == null) {
            Toast.makeText(getContext(), "Error: Receta no recibida", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }

        ingredienteAdapter3 = new IngredienteAdapter3(receta.getIngredientes());
        recyclerIngredientes1.setAdapter(ingredienteAdapter3);

        listaDeIngredienteRecetas = receta.getIngredientes();

        IngredienteSpinnerAdapter ingredienteSpinnerAdapter =
                new IngredienteSpinnerAdapter(requireContext(), listaDeIngredienteRecetas);
        spinnerUnidadIngrediente.setAdapter(ingredienteSpinnerAdapter);

        final IngredienteReceta[] ingredienteSeleccionado = new IngredienteReceta[1];

        // Guardar cantidades originales para cálculo proporcional
        Map<Integer, Double> cantidadesOriginales = new HashMap<>();
        for (int i = 0; i < listaDeIngredienteRecetas.size(); i++) {
            cantidadesOriginales.put(i, listaDeIngredienteRecetas.get(i).getCantidad());
        }

        spinnerUnidadIngrediente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean primeraVez = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (primeraVez) {
                    primeraVez = false;
                    layoutCantidadBotones.setVisibility(View.GONE);
                    ingredienteSeleccionado[0] = null;
                    ingredienteSpinnerAdapter.setSelectedPosition(-1);
                    editCantidad.setText("");
                    txtUnidad.setText("");
                    return;
                }

                if (position == 0) {
                    layoutCantidadBotones.setVisibility(View.GONE);
                    ingredienteSeleccionado[0] = null;
                    ingredienteSpinnerAdapter.setSelectedPosition(-1);
                    editCantidad.setText("");
                    txtUnidad.setText("");
                } else {
                    ingredienteSeleccionado[0] = listaDeIngredienteRecetas.get(position);
                    ingredienteSpinnerAdapter.setSelectedPosition(position);
                    layoutCantidadBotones.setVisibility(View.VISIBLE);

                    editCantidad.setText(String.valueOf((int) ingredienteSeleccionado[0].getCantidad()));
                    txtUnidad.setText(ingredienteSeleccionado[0].getUnidad());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutCantidadBotones.setVisibility(View.GONE);
                ingredienteSeleccionado[0] = null;
                ingredienteSpinnerAdapter.setSelectedPosition(-1);
                editCantidad.setText("");
                txtUnidad.setText("");
            }
        });

        editCantidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ingredienteSeleccionado[0] != null) {
                    try {
                        double nuevaCantidad = Double.parseDouble(s.toString());
                        int indexSeleccionado = listaDeIngredienteRecetas.indexOf(ingredienteSeleccionado[0]);
                        double cantidadOriginal = cantidadesOriginales.get(indexSeleccionado);

                        if (cantidadOriginal > 0) {
                            double factorCambio = (nuevaCantidad - cantidadOriginal) / cantidadOriginal;

                            List<IngredienteReceta> listaVisual = ingredienteAdapter3.getIngredientesEscalados();
                            listaVisual.get(indexSeleccionado).setCantidad(nuevaCantidad);
                            ingredienteSeleccionado[0].setCantidad(nuevaCantidad);

                            for (int i = 0; i < listaVisual.size(); i++) {
                                if (i != indexSeleccionado) {
                                    double cantidadOriginalOtro = cantidadesOriginales.get(i);
                                    double nuevaCantidadOtro = cantidadOriginalOtro + (cantidadOriginalOtro * factorCambio);
                                    listaVisual.get(i).setCantidad(nuevaCantidadOtro);
                                    listaDeIngredienteRecetas.get(i).setCantidad(nuevaCantidadOtro);
                                }
                            }

                            ingredienteSpinnerAdapter.notifyDataSetChanged();
                            ingredienteAdapter3.notifyDataSetChanged();
                        }

                    } catch (NumberFormatException e) {
                        // Ignorar cantidad inválida
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnMas.setOnClickListener(v -> {
            if (ingredienteSeleccionado[0] != null) {
                int indexSeleccionado = listaDeIngredienteRecetas.indexOf(ingredienteSeleccionado[0]);
                List<IngredienteReceta> listaVisual = ingredienteAdapter3.getIngredientesEscalados();

                double cant = listaVisual.get(indexSeleccionado).getCantidad();
                cant++;
                listaVisual.get(indexSeleccionado).setCantidad(cant);
                ingredienteSeleccionado[0].setCantidad(cant);
                listaDeIngredienteRecetas.get(indexSeleccionado).setCantidad(cant);

                editCantidad.setText(String.valueOf((int) cant));

                ingredienteSpinnerAdapter.notifyDataSetChanged();
                ingredienteAdapter3.notifyDataSetChanged();
            }
        });

        btnMenos.setOnClickListener(v -> {
            if (ingredienteSeleccionado[0] != null) {
                int indexSeleccionado = listaDeIngredienteRecetas.indexOf(ingredienteSeleccionado[0]);
                List<IngredienteReceta> listaVisual = ingredienteAdapter3.getIngredientesEscalados();

                double cant = listaVisual.get(indexSeleccionado).getCantidad();
                if (cant > 1) {
                    cant--;
                    listaVisual.get(indexSeleccionado).setCantidad(cant);
                    ingredienteSeleccionado[0].setCantidad(cant);
                    listaDeIngredienteRecetas.get(indexSeleccionado).setCantidad(cant);

                    editCantidad.setText(String.valueOf((int) cant));

                    ingredienteSpinnerAdapter.notifyDataSetChanged();
                    ingredienteAdapter3.notifyDataSetChanged();
                }
            }
        });

        selectTab(0);

        tabEscalado.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectTab(tab.getPosition());

                // Actualizo la variable y hago log para confirmar
                esPorIngredientes = (tab.getPosition() == 1); // 0 = porciones, 1 = ingredientes
                Log.d("PopupEscalar", "esPorIngredientes cambiado a: " + esPorIngredientes);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        editTextPorciones.setText(String.valueOf(receta.getPorciones()));
        editTextPorciones.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int nuevaCantidad = Integer.parseInt(s.toString());
                    int porcionesOriginales = receta.getPorciones();

                    if (porcionesOriginales > 0 && nuevaCantidad > 0) {
                        double factor = (double) nuevaCantidad / porcionesOriginales;
                        ingredienteAdapter3.actualizarCantidadesConFactor((float) factor);
                    }
                } catch (NumberFormatException e) {
                    Log.e("PopupEscalar", "Número inválido de porciones", e);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGuardarVersion.setOnClickListener(v -> guardarVersionCalculada());

        return view;
    }





    private void selectTab(int position) {
        tipoCalculo = position; // 0 = por porciones, 1 = por ingredientes

        if (position == 0) {
            // Por porciones
            layoutPorPorciones.setVisibility(View.VISIBLE);
            recyclerIngredientes1.setVisibility(View.VISIBLE);
            layoutSpinnerUnidad.setVisibility(View.GONE);
            layoutPorIngredientes.setVisibility(View.VISIBLE);
            spinnerUnidadIcon.setVisibility(View.GONE);
        } else {
            // Por ingredientes
            layoutPorPorciones.setVisibility(View.GONE);
            recyclerIngredientes1.setVisibility(View.VISIBLE);
            layoutSpinnerUnidad.setVisibility(View.VISIBLE);
            layoutPorIngredientes.setVisibility(View.VISIBLE);
            spinnerUnidadIcon.setVisibility(View.VISIBLE);
        }

        btnGuardarVersion.setVisibility(View.VISIBLE);

        for (int i = 0; i < tabEscalado.getTabCount(); i++) {
            TabLayout.Tab tab = tabEscalado.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                TextView textView = tab.getCustomView().findViewById(R.id.tabText);
                textView.setTextColor(i == position
                        ? ContextCompat.getColor(requireContext(), android.R.color.white)
                        : ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            }
        }
    }


    private void guardarVersionCalculada() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = sharedPreferences.getLong("id_usuario", -1);

        if (usuarioId == -1) {
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<RecetaCalculadaDTO>> call = apiService.getRecetasCalculadas(usuarioId);

        call.enqueue(new Callback<List<RecetaCalculadaDTO>>() {
            @Override
            public void onResponse(Call<List<RecetaCalculadaDTO>> call, Response<List<RecetaCalculadaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().size() >= 10) {
                        Toast.makeText(getContext(), "Error: Límite de 10 recetas calculadas.", Toast.LENGTH_SHORT).show();
                    } else {
                        continuarGuardadoVersionCalculada(usuarioId);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener recetas calculadas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecetaCalculadaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void continuarGuardadoVersionCalculada(long usuarioId) {
        int nuevasPorciones;
        try {
            nuevasPorciones = Integer.parseInt(editTextPorciones.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Porciones inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreReceta = receta.getNombre() + " (Escalada)";
        Gson gson = new Gson();
        String ingredientesEscaladosJson = gson.toJson(ingredienteAdapter3.getIngredientesEscalados());

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("usuario", Collections.singletonMap("id", usuarioId));
        jsonMap.put("recetaOriginal", Collections.singletonMap("id", receta.getId()));
        jsonMap.put("porciones", nuevasPorciones);
        jsonMap.put("nombre", nombreReceta);
        jsonMap.put("ingredientesEscaladosJson", ingredientesEscaladosJson);
        jsonMap.put("tipoCalculado", esPorIngredientes);

        String jsonFinal = gson.toJson(jsonMap);

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        Log.d("JSON_FINAL", jsonFinal);
        RequestBody requestBody = RequestBody.create(mediaType, jsonFinal);

        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.guardarRecetaEscalada(receta.getId(), requestBody);

        Log.d("TipoCalculado", "Valor enviado: " + esPorIngredientes);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Versión guardada correctamente", Toast.LENGTH_SHORT).show();

                    Map<String, Object> nuevaReceta = new HashMap<>();
                    nuevaReceta.put("nombre", nombreReceta);
                    nuevaReceta.put("autor", "Yo");
                    nuevaReceta.put("porciones", nuevasPorciones);
                    nuevaReceta.put("ingredientesEscalados", ingredienteAdapter3.getIngredientesEscalados());

                    recetasViewModel.agregarRecetaCalculada(nuevaReceta);
                    dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("GuardarReceta", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Error al guardar receta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View createTabView(String title) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_custom_view, null);
        TextView textView = view.findViewById(R.id.tabText);
        textView.setText(title);
        textView.setTextSize(12);
        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        return view;
    }
}
