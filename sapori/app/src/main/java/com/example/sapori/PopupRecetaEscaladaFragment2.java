package com.example.sapori;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteDTO;
import com.example.sapori.model.IngredienteReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class PopupRecetaEscaladaFragment2 extends DialogFragment {

    private static final String TAG = "PopupRecetaEscalada";

    private RecyclerView recyclerIngredientes;
    private RecetaCalculadaDTO receta;
    private TabLayout tabEscalado;

    private Spinner spinnerUnidadIngrediente;
    private FrameLayout layoutSpinnerUnidad;
    private Receta recetaOriginal;

    private RecetasViewModel recetasViewModel;
    private IngredienteAdapter4 ingredienteAdapter4;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PopupDialogTheme);

        Bundle args = getArguments();
        if (args != null) {
            receta = (RecetaCalculadaDTO) args.getSerializable("receta");
            Log.d(TAG, "Receta calculada recibida con " + (receta != null && receta.getIngredientes() != null ? receta.getIngredientes().size() : 0) + " ingredientes");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = 950;
            int height = 1300;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_receta_escalada2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout layoutPorPorciones = view.findViewById(R.id.layoutPorPorciones);
        if (layoutPorPorciones != null) {
            layoutPorPorciones.setVisibility(View.GONE);
        }

        tabEscalado = view.findViewById(R.id.tabEscalado);
        if (tabEscalado != null) {
            tabEscalado.removeAllTabs();
            tabEscalado.addTab(tabEscalado.newTab().setCustomView(createTabView("Por ingredientes")));

            tabEscalado.post(() -> {
                ViewGroup slidingTabStrip = (ViewGroup) tabEscalado.getChildAt(0);
                for (int i = 0; i < slidingTabStrip.getChildCount(); i++) {
                    View tabView = slidingTabStrip.getChildAt(i);
                    if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                        params.leftMargin = 0;
                        params.rightMargin = 0;
                        tabView.setLayoutParams(params);
                    }
                    tabView.setPadding(0, tabView.getPaddingTop(), 0, tabView.getPaddingBottom());
                }
            });
        }

        recyclerIngredientes = view.findViewById(R.id.recyclerIngredientes1);
        recyclerIngredientes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        spinnerUnidadIngrediente = view.findViewById(R.id.spinnerUnidadIngrediente);
        layoutSpinnerUnidad = view.findViewById(R.id.layoutSpinnerUnidad);

        recetasViewModel = new ViewModelProvider(requireActivity()).get(RecetasViewModel.class);

        // Inicializar adapter con ingredientes actuales de receta calculada
        ingredienteAdapter4 = new IngredienteAdapter4(receta != null ? receta.getIngredientes() : new ArrayList<>());
        recyclerIngredientes.setAdapter(ingredienteAdapter4);

        // Observar la recetaOriginal del ViewModel
        recetasViewModel.getRecetaOriginal().observe(getViewLifecycleOwner(), recetaOriginal -> {
            this.recetaOriginal = recetaOriginal;
            if (recetaOriginal != null && receta != null && receta.getIngredientes() != null) {
                completarIngredientesConOriginales(receta, recetaOriginal);
                ingredienteAdapter4.notifyDataSetChanged();
                Log.d(TAG, "Receta original aplicada y adapter notificado");
            }
        });

        if (receta != null && receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()) {
            Log.d(TAG, "Ingredientes escalados iniciales:");
            for (IngredienteDTO dto : receta.getIngredientes()) {
                Log.d(TAG, "Ingrediente: " + dto.getNombre() + ", CantidadOriginal: " + dto.getCantidadOriginal());
            }

            // Spinner: convertir DTO -> IngredienteReceta
            List<IngredienteReceta> ingredientesConvertidos = new ArrayList<>();
            ingredientesConvertidos.add(null); // Primer item vacío

            for (IngredienteDTO dto : receta.getIngredientes()) {
                try {
                    Ingrediente ing = new Ingrediente();
                    ing.setNombre(dto.getNombre());
                    ing.setImagenUrl(dto.getImagenUrl());

                    IngredienteReceta ingReceta = new IngredienteReceta();
                    ingReceta.setIngrediente(ing);
                    ingReceta.setUnidad(dto.getUnidad());

                    double cantidad = Double.parseDouble(dto.getCantidad());
                    ingReceta.setCantidad(cantidad);

                    ingredientesConvertidos.add(ingReceta);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            IngredienteSpinnerAdapter adapter = new IngredienteSpinnerAdapter(requireContext(), ingredientesConvertidos);
            spinnerUnidadIngrediente.setAdapter(adapter);
            layoutSpinnerUnidad.setVisibility(View.VISIBLE);
        } else {
            layoutSpinnerUnidad.setVisibility(View.GONE);
            Log.d(TAG, "No hay ingredientes para mostrar");
        }
    }

    private View createTabView(String title) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_custom_view, null);
        TextView textView = view.findViewById(R.id.tabText);
        textView.setText(title);
        textView.setTextSize(12);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        return view;
    }

    public static void show(@NonNull FragmentManager fragmentManager,
                            @NonNull RecetaCalculadaDTO recetaCalculada) {
        PopupRecetaEscaladaFragment2 fragment = new PopupRecetaEscaladaFragment2();
        Bundle args = new Bundle();
        args.putSerializable("receta", recetaCalculada);
        fragment.setArguments(args);
        fragment.show(fragmentManager, "popup_receta_calculada");
    }

    private void completarIngredientesConOriginales(RecetaCalculadaDTO recetaCalculada, Receta recetaOriginal) {
        if (recetaCalculada == null || recetaOriginal == null) {
            Log.d(TAG, "No se puede completar cantidades, receta calculada o original es null");
            return;
        }

        List<IngredienteDTO> ingredientesEscalados = recetaCalculada.getIngredientes();
        List<IngredienteReceta> ingredientesOriginales = recetaOriginal.getIngredientes();

        Log.d(TAG, "Completando cantidades originales, escalados: " + ingredientesEscalados.size() + ", originales: " + ingredientesOriginales.size());

        for (IngredienteDTO escalado : ingredientesEscalados) {
            for (IngredienteReceta original : ingredientesOriginales) {
                if (original.getIngrediente().getNombre().equalsIgnoreCase(escalado.getNombre())) {
                    escalado.setCantidadOriginal(String.valueOf((int) original.getCantidad()));
                    Log.d(TAG, "Seteando cantidadOriginal para " + escalado.getNombre() + ": " + escalado.getCantidadOriginal());
                    break;
                }
            }
        }
    }
}