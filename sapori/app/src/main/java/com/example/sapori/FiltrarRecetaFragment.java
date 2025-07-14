package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.sapori.IngredientesFragment;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.HashSet;
import java.util.Set;

public class FiltrarRecetaFragment extends Fragment {

    private LinearLayout containerTiposPlato;
    private EditText editBusquedaAutor;

    private Button btnOrdenAZ, btnOrdenAntiguas, btnMasRecientes;
    private String ordenSeleccionado = "";

    private static final String PREFS_NAME = "FiltrosPrefs";
    private static final String KEY_TIPO_SELECCIONADO = "tipo_seleccionado";
    private static final String KEY_AUTOR_SELECCIONADO = "autor_seleccionado";
    private static final String KEY_ORDEN_SELECCIONADO = "orden_seleccionado";

    private final Set<String> tiposSeleccionados = new HashSet<>();

    private String autorEscrito = "";

    private static final String DELIMITADOR = ",";

    private final String[] tiposDePlato = {
            "Entradas", "Ensaladas", "Sandwiches", "Sopas", "Carnes", "Sushi",
            "Hamburguesas", "Pastas", "Pizzas", "Empanadas", "Mariscos", "Tartas",
            "Comida Vegetariana", "Comida Vegana", "Postres", "Bebidas"
    };

    private static final java.util.Map<String, Integer> iconosPorTipo = new java.util.HashMap<>();
    static {
        iconosPorTipo.put("Entradas", R.drawable.entradas);
        iconosPorTipo.put("Ensaladas", R.drawable.ensaladas);
        iconosPorTipo.put("Sandwiches", R.drawable.sandwiches);
        iconosPorTipo.put("Sopas", R.drawable.sopas);
        iconosPorTipo.put("Carnes", R.drawable.carnes);
        iconosPorTipo.put("Sushi", R.drawable.sushi);
        iconosPorTipo.put("Hamburguesas", R.drawable.hamburguesas);
        iconosPorTipo.put("Pastas", R.drawable.pastas);
        iconosPorTipo.put("Pizzas", R.drawable.pizzas);
        iconosPorTipo.put("Empanadas", R.drawable.empanadas);
        iconosPorTipo.put("Mariscos", R.drawable.mariscos);
        iconosPorTipo.put("Tartas", R.drawable.tartas);
        iconosPorTipo.put("Comida Vegetariana", R.drawable.vegetarianos);
        iconosPorTipo.put("Comida Vegana", R.drawable.veganos);
        iconosPorTipo.put("Postres", R.drawable.postres);
        iconosPorTipo.put("Bebidas", R.drawable.bebidas);
    }

    public FiltrarRecetaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filtrar_receta, container, false);

        containerTiposPlato = view.findViewById(R.id.container_tipos_plato);
        editBusquedaAutor = view.findViewById(R.id.edit_busqueda);

        btnOrdenAZ = view.findViewById(R.id.btn_orden_az);
        btnOrdenAntiguas = view.findViewById(R.id.btn_orden_antiguo);
        btnMasRecientes = view.findViewById(R.id.btn_orden_nuevo);



        ImageView btnCerrar = view.findViewById(R.id.btn_cerrar);
        Button btnLimpiar = view.findViewById(R.id.btn_limpiar_filtro);

        Button btnAñadirIngredientes = view.findViewById(R.id.btn_añadir_ingrediente);
        btnAñadirIngredientes.setOnClickListener(v -> {
            IngredientesFragment ingredientesFragment = new IngredientesFragment();
            ingredientesFragment.show(getParentFragmentManager(), "ingredientes_fragment");
        });

        Button btnQuitarIngredientes = view.findViewById(R.id.btn_Quitar_ingrediente);
        btnQuitarIngredientes.setOnClickListener(v -> {
            QuitarIngredientesFragment quitaringredientesFragment = new QuitarIngredientesFragment();
            quitaringredientesFragment.show(getParentFragmentManager(), "quitar_ingredientes_fragment");
        });



        cargarAutorYOrden();
        cargarTiposDePlato();

        btnCerrar.setOnClickListener(v -> {
            guardarSeleccionados();
            Navigation.findNavController(view).navigateUp();
        });

        btnLimpiar.setOnClickListener(v -> {
            tiposSeleccionados.clear();
            autorEscrito = "";
            ordenSeleccionado = "";
            editBusquedaAutor.setText("");
            resetearBotonesOrden();
            cargarTiposDePlato();
            guardarSeleccionados();
        });

        editBusquedaAutor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                autorEscrito = s.toString().trim();
                guardarSeleccionados();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnOrdenAZ.setOnClickListener(v -> actualizarOrden("az"));
        btnOrdenAntiguas.setOnClickListener(v -> actualizarOrden("antiguas"));
        btnMasRecientes.setOnClickListener(v -> actualizarOrden("recientes"));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar ingredientes seleccionados al salir del fragmento
        limpiarIngredientesSeleccionados();
    }

    private void limpiarIngredientesSeleccionados() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ingredientes_prefs2", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.d("FiltrarRecetaFragment", "Ingredientes seleccionados limpiados");
    }

    private void cargarTiposDePlato() {
        containerTiposPlato.removeAllViews();

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String tiposGuardados = prefs.getString(KEY_TIPO_SELECCIONADO, "");
        tiposSeleccionados.clear();

        if (!tiposGuardados.isEmpty()) {
            String[] arr = tiposGuardados.split(DELIMITADOR);
            for (String t : arr) {
                tiposSeleccionados.add(t.trim());
            }
        }

        for (String tipo : tiposDePlato) {
            boolean seleccionado = tiposSeleccionados.contains(tipo);
            LinearLayout itemLayout = crearItemTipoPlato(tipo, seleccionado);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int marginBottomPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    12,  // margen para separar los botones (puedes ajustar este valor)
                    getResources().getDisplayMetrics()
            );
            params.setMargins(0, 0, 0, marginBottomPx);

            itemLayout.setLayoutParams(params);

            containerTiposPlato.addView(itemLayout);
        }
    }

    private void cargarAutorYOrden() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        autorEscrito = prefs.getString(KEY_AUTOR_SELECCIONADO, "");
        ordenSeleccionado = prefs.getString(KEY_ORDEN_SELECCIONADO, "");

        editBusquedaAutor.setText(autorEscrito);

        resetearBotonesOrden();
        if (!ordenSeleccionado.isEmpty()) {
            actualizarVisualBotonesOrden();
        }
    }

    private LinearLayout crearItemTipoPlato(String tipo, boolean seleccionado) {
        Context ctx = requireContext();

        LinearLayout item = new LinearLayout(ctx);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(24, 12, 24, 12);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setBackgroundResource(
                seleccionado ? R.drawable.rectangle_tipo_plato_seleccionado :
                        R.drawable.rectangle_tipo_plato
        );

        // Altura fija en dp convertida a px
        int alturaFijaPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                36,
                ctx.getResources().getDisplayMetrics()
        );

        // Margen horizontal entre botones: 16dp a la derecha
        int margenHorizontalPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                ctx.getResources().getDisplayMetrics()
        );

        LinearLayout.LayoutParams paramsItem = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                alturaFijaPx
        );
        paramsItem.setMargins(0, 0, margenHorizontalPx, 0); // Solo margenEnd
        item.setLayoutParams(paramsItem);

        ImageView icono = new ImageView(ctx);

        Integer iconRes = iconosPorTipo.get(tipo);
        if (iconRes != null) {
            icono.setImageResource(iconRes);
        }

        if (seleccionado) {
            icono.setColorFilter(Color.WHITE);
        } else {
            icono.clearColorFilter();
        }
        icono.setContentDescription(tipo);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
        iconParams.setMargins(0, 0, 12, 0);
        item.addView(icono, iconParams);

        TextView texto = new TextView(ctx);
        texto.setText(tipo);
        texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        texto.setTextColor(seleccionado ? Color.WHITE : Color.parseColor("#5E5E5E"));
        texto.setGravity(Gravity.CENTER_VERTICAL);

// Agregar esta línea para cargar la fuente personalizada
        Typeface typeface = ResourcesCompat.getFont(ctx, R.font.poppinsregular);
        texto.setTypeface(typeface);

        LinearLayout.LayoutParams textoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        texto.setLayoutParams(textoParams);

        item.addView(texto);

        item.setOnClickListener(v -> {
            if (tiposSeleccionados.contains(tipo)) {
                tiposSeleccionados.remove(tipo);
                item.setBackgroundResource(R.drawable.rectangle_tipo_plato);
                texto.setTextColor(Color.parseColor("#5E5E5E"));
                icono.clearColorFilter();
            } else {
                tiposSeleccionados.add(tipo);
                item.setBackgroundResource(R.drawable.rectangle_tipo_plato_seleccionado);
                texto.setTextColor(Color.WHITE);
                icono.setColorFilter(Color.WHITE);
            }
            guardarSeleccionados();
        });

        return item;
    }

    private void actualizarOrden(String nuevoOrden) {
        ordenSeleccionado = nuevoOrden;
        actualizarVisualBotonesOrden();
        guardarSeleccionados();
    }

    private void actualizarVisualBotonesOrden() {
        resetearBotonesOrden();

        Button botonSeleccionado = null;
        switch (ordenSeleccionado) {
            case "az":
                botonSeleccionado = btnOrdenAZ;
                break;
            case "antiguas":
                botonSeleccionado = btnOrdenAntiguas;
                break;
            case "recientes":
                botonSeleccionado = btnMasRecientes;
                break;
        }

        if (botonSeleccionado != null) {
            Drawable iconoCheck = ContextCompat.getDrawable(requireContext(), R.drawable.icono_check_small);
            if (iconoCheck != null) {
                botonSeleccionado.setCompoundDrawablesRelativeWithIntrinsicBounds(iconoCheck, null, null, null);

                botonSeleccionado.setCompoundDrawablePadding(12);
                botonSeleccionado.setPadding(20,
                        botonSeleccionado.getPaddingTop(),
                        botonSeleccionado.getPaddingRight(),
                        botonSeleccionado.getPaddingBottom());

                botonSeleccionado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E8137")));
                botonSeleccionado.setTextColor(Color.WHITE);
            }
        }
    }

    private void resetearBotonesOrden() {
        Button[] botones = {btnOrdenAZ, btnOrdenAntiguas, btnMasRecientes};

        for (Button boton : botones) {
            boton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            boton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E5E5E5")));
            boton.setTextColor(Color.parseColor("#7C7C7C"));
            boton.setPadding(40, boton.getPaddingTop(), 40, boton.getPaddingBottom());
        }
    }

    private void guardarSeleccionados() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder sb = new StringBuilder();
        for (String tipo : tiposSeleccionados) {
            if (sb.length() > 0) sb.append(DELIMITADOR);
            sb.append(tipo);
        }

        String tiposGuardados = sb.toString();

        editor.putString(KEY_TIPO_SELECCIONADO, tiposGuardados);
        editor.putString(KEY_AUTOR_SELECCIONADO, autorEscrito);
        editor.putString(KEY_ORDEN_SELECCIONADO, ordenSeleccionado);
        editor.apply();

        // 🔍 LOG para verificar lo que se guarda
        Log.d("FiltrarReceta", "Tipos seleccionados: " + tiposGuardados);
        Log.d("FiltrarReceta", "Autor escrito: " + autorEscrito);
        Log.d("FiltrarReceta", "Orden seleccionado: " + ordenSeleccionado);
    }
}
