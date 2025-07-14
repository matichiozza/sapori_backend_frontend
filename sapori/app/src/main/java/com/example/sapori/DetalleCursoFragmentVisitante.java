package com.example.sapori;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.sapori.R;
import com.example.sapori.model.Curso;
import com.example.sapori.model.PracticaCursoDTO;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCursoFragmentVisitante extends Fragment {

    private LinearLayout containerBlur;

    private LinearLayout layoutDots;
    private ViewPager2 viewPagerImagenes;
    private TextView txtTitulo, txtDescripcion, txtPorciones, btnSedesDispoTexto;
    private ImageButton flecha;
    private ImageView logoPresencial, logoNuevo;
    private TextView modalidad, modalidad2, modalidad3, txtPrecio;
    private TextView txtObjetivo, txtObjetivo1, txtDescripcion1, txtTemario;
    private LinearLayout layoutTemario;
    private TextView txtUtensillosLista;
    private LinearLayout layoutComentarios;
    private TextView txtRequisitos, txtPracticas;
    private LinearLayout layoutPracticas;

    private ApiService apiService;
    private Curso curso;

    private boolean desdeGestionar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detallecurso_visitante, container, false);

        containerBlur = view.findViewById(R.id.containerBlur);

        layoutDots = view.findViewById(R.id.indicatorLayout);
        viewPagerImagenes = view.findViewById(R.id.viewPagerImagenes);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtPorciones = view.findViewById(R.id.txtPorciones);
        logoPresencial = view.findViewById(R.id.logo_presencial);
        logoNuevo = view.findViewById(R.id.logo_nuevo);
        modalidad = view.findViewById(R.id.modalidad);
        modalidad2 = view.findViewById(R.id.modalidad2);
        modalidad3 = view.findViewById(R.id.modalidad3);
        txtPrecio = view.findViewById(R.id.txtPrecio);
        txtObjetivo = view.findViewById(R.id.txtObjetivo);
        txtObjetivo1 = view.findViewById(R.id.txtObjetivo1);
        txtDescripcion1 = view.findViewById(R.id.txtDescripcion1);
        txtTemario = view.findViewById(R.id.txtTemario);
        layoutTemario = view.findViewById(R.id.layoutTemario);
        txtUtensillosLista = view.findViewById(R.id.txtUtensillosLista);
        layoutComentarios = view.findViewById(R.id.layoutComentarios);
        txtRequisitos = view.findViewById(R.id.txtRequisitos);
        txtPracticas = view.findViewById(R.id.txtPracticas);
        layoutPracticas = view.findViewById(R.id.layoutPracticas);
        flecha = view.findViewById(R.id.btnAtras);

        // Aplico blur si la versión es Android 12 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float blurRadius = 20f;
            containerBlur.setRenderEffect(RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP));
        }

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        if (getArguments() != null) {
            desdeGestionar = getArguments().getBoolean("desdeGestionar", false);
        }

        Long cursoId = null;
        Bundle args = getArguments();
        if (args != null && args.containsKey("cursoId")) {
            cursoId = args.getLong("cursoId");
        }

        if (cursoId != null && cursoId != 0) {
            apiService = ApiClient.getApiService();
            obtenerCursoDesdeBackend(cursoId);
        } else {
            Toast.makeText(requireContext(), "No se recibió información del curso", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void obtenerCursoDesdeBackend(Long cursoId) {
        Call<Curso> call = apiService.obtenerCurso(cursoId);

        call.enqueue(new Callback<Curso>() {
            @Override
            public void onResponse(@NonNull Call<Curso> call, @NonNull Response<Curso> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    curso = response.body();
                    Log.d("CursoDebug", "Fotos recibidas: " + curso.getFotosUrl());
                    cargarDatosEnVista();
                } else {
                    Log.e("API", "Error en respuesta: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar curso", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Curso> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Error de conexión", t);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosEnVista() {
        if (curso == null) return;

        // Título y descripción
        txtTitulo.setText(curso.getNombre() != null ? curso.getNombre() : "");
        txtDescripcion1.setText(curso.getDescripcion() != null ? curso.getDescripcion() : "");

        // Duración y precio
        txtPorciones.setText(curso.getDuracion() + " semanas");
        txtPrecio.setText(curso.getImporte() != null ? "$" + curso.getImporte() : "Sin precio");

        // Objetivo
        if (curso.getObjetivo() != null && !curso.getObjetivo().isEmpty()) {
            txtObjetivo.setVisibility(View.VISIBLE);
            txtObjetivo1.setVisibility(View.VISIBLE);
            txtObjetivo1.setText(curso.getObjetivo());
        } else {
            txtObjetivo.setVisibility(View.GONE);
            txtObjetivo1.setVisibility(View.GONE);
        }

        // Adapter imágenes
        ImagenURLAdapter imagenAdapter = new ImagenURLAdapter(curso.getFotosUrl());
        viewPagerImagenes.setAdapter(imagenAdapter);

        // Modalidad
        String modalidadCurso = curso.getModalidad() != null ? curso.getModalidad().toLowerCase() : "";
        logoPresencial.setVisibility(View.GONE);
        modalidad.setVisibility(View.VISIBLE);
        logoNuevo.setVisibility(View.GONE);
        modalidad3.setVisibility(View.GONE);

        if (modalidadCurso.contains("presencial")) {
            logoPresencial.setVisibility(View.VISIBLE);
            modalidad2.setVisibility(View.VISIBLE);
        } else if (modalidadCurso.contains("virtual")) {
            logoNuevo.setVisibility(View.VISIBLE);
            modalidad3.setVisibility(View.VISIBLE);
        }

        // Indicadores para ViewPager
        agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, 0);

        viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, position);
            }
        });

        // Temario
        txtTemario.setText("Temario");
        layoutTemario.removeAllViews();
        String temarioCompleto = curso.getTemario() != null ? curso.getTemario() : "";
        String[] lineasTemario = temarioCompleto.split("\n");
        Typeface fuenteTemario = ResourcesCompat.getFont(requireContext(), R.font.poppinssemibolditalic);

        for (String linea : lineasTemario) {
            if (linea.trim().isEmpty()) continue;

            TextView tvLinea = new TextView(requireContext());
            tvLinea.setTypeface(fuenteTemario);
            tvLinea.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvLinea.setPadding(0, dpToPx(4), 0, dpToPx(4));

            String trimmed = linea.trim();
            if (trimmed.matches("^\\d+\\..*")) {
                int indexEspacio = trimmed.indexOf(' ');
                int endIndex = indexEspacio > 0 ? indexEspacio : trimmed.indexOf('.') + 1;

                String numero = trimmed.substring(0, endIndex).trim();
                String resto = trimmed.substring(endIndex).trim();

                SpannableString spannable = new SpannableString(numero + " " + resto);
                spannable.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#2E8137")),
                        0,
                        numero.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannable.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#9D9D9D")),
                        numero.length(),
                        spannable.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                tvLinea.setText(spannable);
            } else {
                tvLinea.setTextColor(Color.parseColor("#9D9D9D"));
                tvLinea.setText(trimmed);
            }

            layoutTemario.addView(tvLinea);
        }

        // Utensilios (requisitos)
        String tituloUtensilios = "Utensillos:";
        String utensilios = curso.getRequisitos() != null ? curso.getRequisitos().trim() : "";
        String textoCompleto = tituloUtensilios + " " + utensilios;

        Typeface fuenteUtensiliosTitulo = ResourcesCompat.getFont(requireContext(), R.font.poppinsbold);
        Typeface fuenteUtensiliosContenido = ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium);

        SpannableString spannableUtensilios = new SpannableString(textoCompleto);

        txtUtensillosLista.setText(spannableUtensilios);
        txtUtensillosLista.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // Prácticas
        txtPracticas.setText("Prácticas a realizar:");
        layoutPracticas.removeAllViews();

        List<PracticaCursoDTO> practicas = curso.getPracticas();
        Typeface fuenteTitulosPractica = ResourcesCompat.getFont(requireContext(), R.font.poppinssemibold);
        Typeface fuentePersonalizada = ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium);

        if (practicas != null && !practicas.isEmpty()) {
            for (PracticaCursoDTO practica : practicas) {
                LinearLayout itemLayout = new LinearLayout(requireContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 0, dpToPx(14));
                itemLayout.setLayoutParams(layoutParams);
                itemLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));

                // Layout horizontal para icono + título
                LinearLayout tituloLayout = new LinearLayout(requireContext());
                tituloLayout.setOrientation(LinearLayout.HORIZONTAL);
                tituloLayout.setGravity(Gravity.CENTER_VERTICAL);

                ImageView icono = new ImageView(requireContext());
                int sizeIcon = dpToPx(19);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizeIcon, sizeIcon);
                iconParams.setMargins(0, 0, dpToPx(8), 0);
                icono.setLayoutParams(iconParams);

                if (practica.getIconoTituloUrl() != null && !practica.getIconoTituloUrl().isEmpty()) {
                    Glide.with(requireContext())
                            .load(practica.getIconoTituloUrl())
                            .into(icono);
                } else {
                    icono.setImageDrawable(null);
                }

                TextView tituloTextView = new TextView(requireContext());
                String titulo = practica.getTitulo() + ":";
                SpannableString spannableTitulo = new SpannableString(titulo);
                spannableTitulo.setSpan(new ForegroundColorSpan(Color.parseColor("#2E8137")), 0, titulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tituloTextView.setText(spannableTitulo);
                tituloTextView.setTypeface(fuentePersonalizada);
                tituloTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                tituloLayout.addView(icono);
                tituloLayout.addView(tituloTextView);

                // Lista debajo del título
                TextView listaTextView = new TextView(requireContext());
                String listaFormateada = practica.getLista() != null ? practica.getLista().replace("\\n", "\n") : "";
                String[] items = listaFormateada.split("\n");

                StringBuilder sb = new StringBuilder();
                for (String item : items) {
                    item = item.trim();
                    if (!item.isEmpty()) {
                        sb.append(item).append("\n");
                    }
                }

                String listaTexto = sb.toString().trim();
                SpannableString spannableLista = new SpannableString(listaTexto);
                spannableLista.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), 0, spannableLista.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                listaTextView.setText(spannableLista);
                listaTextView.setTypeface(fuentePersonalizada);
                listaTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                listaTextView.setPadding(dpToPx(32), 0, 0, 0);

                itemLayout.addView(tituloLayout);
                itemLayout.addView(listaTextView);

                layoutPracticas.addView(itemLayout);
            }
        }
    }

    private int dpToPx(int dp) {
        if (getResources() == null) return dp;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void agregarIndicadoresDots(int totalDots, int posicionActual) {
        if (layoutDots == null) return;

        layoutDots.removeAllViews();

        if (totalDots <= 0) return;

        for (int i = 0; i < totalDots; i++) {
            View dot = new View(requireContext());
            int size = dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);

            if (i == posicionActual) {
                dot.setBackgroundResource(R.drawable.dot_selected); // Asegúrate que tienes este drawable
            } else {
                dot.setBackgroundResource(R.drawable.dot_unselected);
            }

            layoutDots.addView(dot);
        }
    }
}
