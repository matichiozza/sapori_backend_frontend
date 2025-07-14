package com.example.sapori;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.google.android.material.tabs.TabLayout;

public class PopupRecetaEscaladaFragment extends DialogFragment {

    private TabLayout tabEscalado;
    private LinearLayout layoutPorPorciones;
    private EditText editTextPorciones;
    private RecyclerView recyclerIngredientes1;

    private RecetasViewModel recetasViewModel;
    private IngredienteAdapter4 ingredienteAdapter4;
    private RecetaCalculadaDTO receta;

    private Receta recetaOriginal;  // <--- agregalo aquí

    public PopupRecetaEscaladaFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PopupDialogTheme);

        Bundle args = getArguments();
        if (args != null) {
            receta = (RecetaCalculadaDTO) args.getSerializable("receta");
            recetaOriginal = (Receta) args.getSerializable("recetaOriginal");  // <-- acá lo agregás
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
        // Solo inflar layout normal
        return inflater.inflate(R.layout.popup_receta_escalada, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (receta != null) {
            Log.d("PopupRecetaEscalada", "tipoCalculado: " + receta.getTipoCalculado());
        } else {
            Log.d("PopupRecetaEscalada", "receta es null");
        }

        if (receta != null && Boolean.TRUE.equals(receta.getTipoCalculado())) {
            Log.d("PopupRecetaEscalada", "Abriendo PopupRecetaEscaladaFragment2 porque tipoCalculado es true");
            // Llamar solo con receta calculada, recetaOriginal se obtiene desde el ViewModel
            PopupRecetaEscaladaFragment2.show(requireActivity().getSupportFragmentManager(), receta);
            dismiss();
            return;
        } else {
            Log.d("PopupRecetaEscalada", "Abriendo popup normal porque tipoCalculado es false o null");
        }

        // Inicializaciones normales con view.findViewById...
        tabEscalado = view.findViewById(R.id.tabEscalado);
        layoutPorPorciones = view.findViewById(R.id.layoutPorPorciones);
        editTextPorciones = view.findViewById(R.id.editTextPorciones);
        recyclerIngredientes1 = view.findViewById(R.id.recyclerIngredientes1);

        recetasViewModel = new ViewModelProvider(requireActivity()).get(RecetasViewModel.class);

        recyclerIngredientes1.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        tabEscalado.addTab(tabEscalado.newTab().setCustomView(createTabView("Por porciones")));
        selectTab(0);

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

        tabEscalado.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { selectTab(tab.getPosition()); }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view1 = tab.getCustomView();
                if (view1 != null) {
                    TextView textView = view1.findViewById(R.id.tabText);
                    textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        ingredienteAdapter4 = new IngredienteAdapter4(receta.getIngredientes());
        recyclerIngredientes1.setAdapter(ingredienteAdapter4);

        editTextPorciones.setText(String.valueOf(receta.getPorciones()));

        editTextPorciones.setFocusable(true);
        editTextPorciones.setClickable(true);
        editTextPorciones.setCursorVisible(true);
        editTextPorciones.setInputType(InputType.TYPE_CLASS_NUMBER);

        editTextPorciones.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int nuevasPorciones = Integer.parseInt(s.toString());
                    int porcionesOriginales = receta.getPorciones();

                    if (porcionesOriginales > 0 && nuevasPorciones > 0) {
                        float factor = (float) nuevasPorciones / porcionesOriginales;
                        ingredienteAdapter4.actualizarCantidadesConFactor(factor);
                    }
                } catch (NumberFormatException e) {
                    Log.e("PopupRecetaEscalada", "Número inválido de porciones", e);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private View createTabView(String title) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_custom_view, null);
        TextView textView = view.findViewById(R.id.tabText);
        textView.setText(title);
        textView.setTextSize(12);
        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        Typeface fuentePersonalizada = ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium);
        textView.setTypeface(fuentePersonalizada);
        return view;
    }

    private void selectTab(int position) {
        if (position == 0) {
            editTextPorciones.setVisibility(View.VISIBLE);
        } else {
            editTextPorciones.setVisibility(View.GONE);
        }

        for (int i = 0; i < tabEscalado.getTabCount(); i++) {
            TabLayout.Tab tab = tabEscalado.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                TextView textView = tab.getCustomView().findViewById(R.id.tabText);
                textView.setTextColor(i == position
                        ? getResources().getColor(android.R.color.white)
                        : getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
}