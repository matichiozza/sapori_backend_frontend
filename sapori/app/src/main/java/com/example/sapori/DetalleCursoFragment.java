package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Curso;
import com.example.sapori.model.PracticaCurso;
import com.example.sapori.model.PracticaCursoDTO;
import com.example.sapori.model.SedeVacanteResponse;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCursoFragment extends Fragment {

    // Declaración de variables
    private ViewPager2 viewPagerImagenes;
    private boolean desdeGestionar = false;
    private TextView txtTitulo, txtDescripcion, txtPorciones, btnSedesDispoTexto, modalidad, modalidad2, modalidad3, txtPrecio;
    private LinearLayout layoutDots;
    private TextView txtObjetivo, txtObjetivo1, txtDescripcion1, txtTemario;
    private LinearLayout layoutTemario;
    private TextView txtRequisitos, txtRequisitos1, txtPracticas, txtUtensillosLista;

    private LinearLayout layoutComentarios;

    // NUEVA variable para prácticas
    private LinearLayout layoutPracticas;

    private ImageView logoPresencial, logoNuevo;

    private ImageView flecha;
    private Curso curso;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_curso, container, false);

        if (getArguments() != null) {
            desdeGestionar = getArguments().getBoolean("desdeGestionar", false);
        }

        layoutDots = view.findViewById(R.id.indicatorLayout);
        viewPagerImagenes = view.findViewById(R.id.viewPagerImagenes);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtPorciones = view.findViewById(R.id.txtPorciones);
        btnSedesDispoTexto = view.findViewById(R.id.btnSedesDispoTexto);
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

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Mejor validación del cursoId recibido:
        Bundle args = getArguments();
        Long cursoId = null;
        if (args != null && args.containsKey("cursoId")) {
            cursoId = args.getLong("cursoId");
        }
        Log.d("DetalleCurso", "ID recibido: " + cursoId);

        if (cursoId != null && cursoId != 0) {
            apiService = ApiClient.getApiService();
            obtenerCursoDesdeBackend(cursoId);
        } else {
            Log.e("DetalleCurso", "No se recibió un cursoId válido");
            Toast.makeText(requireContext(), "No se recibió información del curso", Toast.LENGTH_SHORT).show();
        }

        btnSedesDispoTexto.setOnClickListener(v -> {
            if (curso != null && curso.getId() != null) {
                ApiService apiService = ApiClient.getApiService();
                Call<List<SedeVacanteResponse>> call = apiService.obtenerSedesPorCurso(curso.getId());

                call.enqueue(new Callback<List<SedeVacanteResponse>>() {
                    @Override
                    public void onResponse(Call<List<SedeVacanteResponse>> call, Response<List<SedeVacanteResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarPopupSedes(response.body());
                        } else {
                            Toast.makeText(getContext(), "No se pudieron obtener las sedes", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<SedeVacanteResponse>> call, Throwable t) {
                        Toast.makeText(getContext(), "Error de red al obtener sedes", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Curso no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // --- INSCRIBIRSE ---
        TextView btnInscribirse = view.findViewById(R.id.btnInscribirse);
        Long finalCursoId = cursoId; // para usar en el listener
        btnInscribirse.setOnClickListener(v -> {
            if (finalCursoId != null && finalCursoId != 0) {
                Bundle bundle = new Bundle();
                bundle.putLong("cursoId", finalCursoId);
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_detalleCursoFragment_to_seleccionarSedeFragment, bundle);
            } else {
                Toast.makeText(requireContext(), "No se recibió información del curso", Toast.LENGTH_SHORT).show();
            }
        });

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
                    cargarDatosEnVista();

                    SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                    long usuarioId = userPrefs.getLong("id_usuario", -1);

                    if (usuarioId == -1) {
                        Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
                    }

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

    private View crearLineaDivisoria() {
        View linea = new View(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()) // 2dp de alto
        );
        // Márgenes para centrar la línea en el espacio
        params.setMargins(0, 24, 0, 24);
        linea.setLayoutParams(params);
        linea.setBackgroundColor(Color.parseColor("#D3D3D3")); // gris oscuro
        return linea;
    }

    private void cargarDatosEnVista() {
        Log.d("DetalleCursoFragment", "Iniciando carga de datos en vista");

        txtTitulo.setText(curso.getNombre());
        Log.d("DetalleCursoFragment", "Título: " + curso.getNombre());

        txtDescripcion.setText("Descripción");
        txtDescripcion1.setText(curso.getDescripcion() != null ? curso.getDescripcion() : "");
        Log.d("DetalleCursoFragment", "Descripción: " + curso.getDescripcion());

        // Agrego línea divisoria después de descripción
        ((ViewGroup) txtDescripcion1.getParent()).addView(crearLineaDivisoria(),
                ((ViewGroup) txtDescripcion1.getParent()).indexOfChild(txtDescripcion1) + 1);

        txtPorciones.setText(curso.getDuracion() + " semanas");
        txtPrecio.setText(curso.getImporte() != null ? "$" + curso.getImporte() : "Sin precio");
        Log.d("DetalleCursoFragment", "Duración: " + curso.getDuracion() + ", Precio: " + curso.getImporte());

        ImagenURLAdapter imagenAdapter = new ImagenURLAdapter(curso.getFotosUrl());
        viewPagerImagenes.setAdapter(imagenAdapter);
        Log.d("DetalleCursoFragment", "Adapter de imágenes seteado con " + (curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0) + " imágenes");

        Typeface fuentePersonalizada = ResourcesCompat.getFont(getContext(), R.font.poppinsmedium);
        Typeface fuenteTemario = ResourcesCompat.getFont(getContext(), R.font.poppinssemibolditalic);
        Typeface fuenteUtensiliosTitulo = ResourcesCompat.getFont(getContext(), R.font.poppinsbold);
        Typeface fuenteUtensiliosContenido = ResourcesCompat.getFont(getContext(), R.font.poppinsmedium);
        Typeface fuenteTitulosPractica = ResourcesCompat.getFont(getContext(), R.font.poppinssemibold);

        txtObjetivo.setText("Objetivo");
        txtObjetivo1.setText(curso.getObjetivo() != null ? curso.getObjetivo() : "");
        Log.d("DetalleCursoFragment", "Objetivo: " + curso.getObjetivo());

        // Agrego línea divisoria después de objetivo
        ((ViewGroup) txtObjetivo1.getParent()).addView(crearLineaDivisoria(),
                ((ViewGroup) txtObjetivo1.getParent()).indexOfChild(txtObjetivo1) + 1);

        txtTemario.setText("Temario");
        layoutTemario.removeAllViews();

        String temarioCompleto = curso.getTemario() != null ? curso.getTemario() : "";
        String[] lineasTemario = temarioCompleto.split("\n");
        Log.d("DetalleCursoFragment", "Temario líneas: " + lineasTemario.length);

        for (String linea : lineasTemario) {
            if (linea.trim().isEmpty()) continue;

            TextView tvLinea = new TextView(getContext());
            tvLinea.setTypeface(fuenteTemario);
            tvLinea.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvLinea.setPadding(0, 4, 0, 4);

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

        // Agrego línea divisoria después del temario (suponiendo layoutTemario está en un contenedor)
        if (layoutTemario.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) layoutTemario.getParent();
            parent.addView(crearLineaDivisoria(), parent.indexOfChild(layoutTemario) + 1);
        }

        String tituloUtensilios = "Utensillos:";
        String utensilios = curso.getRequisitos() != null ? curso.getRequisitos().trim() : "";
        String textoCompleto = tituloUtensilios + " " + utensilios;

        SpannableString spannableUtensilios = new SpannableString(textoCompleto);
        spannableUtensilios.setSpan(new CustomTypefaceSpan(fuenteUtensiliosTitulo), 0, tituloUtensilios.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), 0, tituloUtensilios.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new CustomTypefaceSpan(fuenteUtensiliosContenido), tituloUtensilios.length() + 1, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), tituloUtensilios.length() + 1, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtUtensillosLista.setText(spannableUtensilios);
        txtUtensillosLista.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        Log.d("DetalleCursoFragment", "Utensilios: " + utensilios);

        // Agrego línea divisoria después de utensilios
        ((ViewGroup) txtUtensillosLista.getParent()).addView(crearLineaDivisoria(),
                ((ViewGroup) txtUtensillosLista.getParent()).indexOfChild(txtUtensillosLista) + 1);

        // Prácticas
        txtPracticas.setText("Prácticas a realizar:");
        layoutPracticas.removeAllViews();

        List<PracticaCursoDTO> practicas = curso.getPracticas();
        Log.d("DetalleCursoFragment", "Número de prácticas: " + (practicas != null ? practicas.size() : "null"));

        if (practicas != null && !practicas.isEmpty()) {
            for (PracticaCursoDTO practica : practicas) {
                LinearLayout itemLayout = new LinearLayout(getContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);  // vertical para separar título+icono de lista

                // Margen inferior para separar prácticas
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 0, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics())); // 16dp abajo
                itemLayout.setLayoutParams(layoutParams);

                itemLayout.setPadding(0, 8, 0, 8);

                // --- Layout horizontal para icono + título ---
                LinearLayout tituloLayout = new LinearLayout(getContext());
                tituloLayout.setOrientation(LinearLayout.HORIZONTAL);
                tituloLayout.setGravity(Gravity.CENTER_VERTICAL);

                ImageView icono = new ImageView(getContext());
                int sizeIcon = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getResources().getDisplayMetrics());
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizeIcon, sizeIcon);
                iconParams.setMargins(0, 0, 8, 0); // margen a la derecha
                icono.setLayoutParams(iconParams);

                if (practica.getIconoTituloUrl() != null && !practica.getIconoTituloUrl().isEmpty()) {
                    Glide.with(getContext())
                            .load(practica.getIconoTituloUrl())
                            .into(icono);
                } else {
                    icono.setImageDrawable(null);
                }

                TextView tituloTextView = new TextView(getContext());
                String titulo = practica.getTitulo() + ":";
                SpannableString spannableTitulo = new SpannableString(titulo);
                spannableTitulo.setSpan(new CustomTypefaceSpan(fuenteTitulosPractica), 0, titulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableTitulo.setSpan(new ForegroundColorSpan(Color.parseColor("#2E8137")), 0, titulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tituloTextView.setText(spannableTitulo);
                tituloTextView.setTypeface(fuentePersonalizada);
                tituloTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                tituloLayout.addView(icono);
                tituloLayout.addView(tituloTextView);

                // --- TextView para lista debajo ---
                TextView listaTextView = new TextView(getContext());
                String listaFormateada = practica.getLista().replace("\\n", "\n");
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
                spannableLista.setSpan(new CustomTypefaceSpan(fuentePersonalizada), 0, spannableLista.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableLista.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), 0, spannableLista.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                listaTextView.setText(spannableLista);
                listaTextView.setTypeface(fuentePersonalizada);
                listaTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                listaTextView.setPadding(32, 0, 0, 0); // indentado para la lista

                // Agrego al layout principal vertical
                itemLayout.addView(tituloLayout);
                itemLayout.addView(listaTextView);

                layoutPracticas.addView(itemLayout);
            }
        }

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

        agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, 0);

        viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, position);
            }
        });

        Log.d("DetalleCursoFragment", "Carga de datos completada");

        // --- Lógica para el botón de inscripción ---
        TextView btnInscribirse = requireView().findViewById(R.id.btnInscribirse);
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = userPrefs.getLong("id_usuario", -1);
        
        Log.d("DetalleCursoFragment", "Usuario ID: " + usuarioId);
        Log.d("DetalleCursoFragment", "Curso ID: " + curso.getId());
        Log.d("DetalleCursoFragment", "Inscriptos en curso: " + (curso.getInscriptos() != null ? curso.getInscriptos().size() : "null"));
        
        boolean yaInscripto = false;
        
        // Primera verificación: usando la lista de inscriptos del curso
        if (curso.getInscriptos() != null && usuarioId != -1) {
            for (com.example.sapori.model.Alumno alumno : curso.getInscriptos()) {
                Log.d("DetalleCursoFragment", "Verificando alumno: " + (alumno != null ? alumno.getId() : "null"));
                if (alumno != null && alumno.getId() != null && alumno.getId().equals(usuarioId)) {
                    yaInscripto = true;
                    Log.d("DetalleCursoFragment", "Usuario encontrado en lista de inscriptos");
                    break;
                }
            }
        }
        
        // Segunda verificación: usando el endpoint de cursos del alumno
        if (!yaInscripto && usuarioId != -1) {
            verificarInscripcionAlumno(usuarioId, curso.getId(), btnInscribirse);
        } else {
            actualizarBotonInscripcion(btnInscribirse, yaInscripto);
        }
    }
    
    private void verificarInscripcionAlumno(long usuarioId, Long cursoId, TextView btnInscribirse) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.getCursosPorAlumno(usuarioId);
        
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean yaInscripto = false;
                    List<Map<String, Object>> cursosAlumno = response.body();
                    
                    Log.d("DetalleCursoFragment", "Cursos del alumno: " + cursosAlumno.size());
                    
                    for (Map<String, Object> cursoAlumno : cursosAlumno) {
                        Object idObj = cursoAlumno.get("cursoId");
                        if (idObj != null) {
                            Long idCursoAlumno;
                            if (idObj instanceof Number) {
                                idCursoAlumno = ((Number) idObj).longValue();
                            } else {
                                try {
                                    idCursoAlumno = Long.parseLong(idObj.toString());
                                } catch (NumberFormatException e) {
                                    continue;
                                }
                            }
                            
                            Log.d("DetalleCursoFragment", "Comparando curso alumno: " + idCursoAlumno + " con curso actual: " + cursoId);
                            
                            if (idCursoAlumno.equals(cursoId)) {
                                yaInscripto = true;
                                Log.d("DetalleCursoFragment", "Usuario está inscripto en este curso");
                                break;
                            }
                        }
                    }
                    
                    actualizarBotonInscripcion(btnInscribirse, yaInscripto);
                } else {
                    Log.d("DetalleCursoFragment", "No se pudieron obtener los cursos del alumno");
                    actualizarBotonInscripcion(btnInscribirse, false);
                }
            }
            
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("DetalleCursoFragment", "Error al verificar inscripción: " + t.getMessage());
                actualizarBotonInscripcion(btnInscribirse, false);
            }
        });
    }
    
    private void actualizarBotonInscripcion(TextView btnInscribirse, boolean yaInscripto) {
        if (yaInscripto) {
            btnInscribirse.setText("Ya estás inscripto");
            btnInscribirse.setClickable(false);
            btnInscribirse.setFocusable(false);
            btnInscribirse.setBackgroundResource(R.drawable.bg_rectangle_grey);
            btnInscribirse.setTextColor(Color.parseColor("#242424"));
            Log.d("DetalleCursoFragment", "Botón actualizado: Ya estás inscripto");
        } else {
            btnInscribirse.setText("INSCRIBIRSE");
            btnInscribirse.setClickable(true);
            btnInscribirse.setFocusable(true);
            btnInscribirse.setBackgroundResource(R.drawable.bg_rectangle_green);
            btnInscribirse.setTextColor(Color.WHITE);
            Log.d("DetalleCursoFragment", "Botón actualizado: INSCRIBIRSE");
        }
    }

    private void mostrarPopupSedes(java.util.List<com.example.sapori.model.SedeVacanteResponse> sedes) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.popup_sedes, null);
        LinearLayout layoutSedes = view.findViewById(R.id.layoutSedes);

        for (int i = 0; i < sedes.size(); i++) {
            SedeVacanteResponse sede = sedes.get(i);
            View sedeView = LayoutInflater.from(getContext()).inflate(R.layout.item_sede_vacante, null);
            TextView nombre = sedeView.findViewById(R.id.txtNombreSede);
            TextView vacantes = sedeView.findViewById(R.id.txtVacantes);

            nombre.setText(sede.getNombreSede());
            vacantes.setText(sede.getVacantesDisponibles() + " vacantes");

            // Colores según sede (igual que antes)
            String nombreSede = sede.getNombreSede().toLowerCase();
            if (nombreSede.contains("centro")) {
                sedeView.setBackgroundResource(R.drawable.bg_sede_item_centro);
                vacantes.setTextColor(Color.parseColor("#828282"));
            } else if (nombreSede.contains("bernal")) {
                sedeView.setBackgroundResource(R.drawable.bg_sede_item_bernal);
                vacantes.setTextColor(Color.parseColor("#828282"));
            } else if (nombreSede.contains("merlo")) {
                sedeView.setBackgroundResource(R.drawable.bg_sede_item_merlo);
                vacantes.setTextColor(Color.parseColor("#828282"));
            } else {
                sedeView.setBackgroundResource(R.drawable.bg_sede_item);
            }

            // Margen inferior solo si NO es el último item
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < sedes.size() - 1) {
                params.setMargins(0, 0, 0, 32); // 32px de separación entre items (ajusta a gusto)
            }
            sedeView.setLayoutParams(params);

            layoutSedes.addView(sedeView);
        }

        builder.setView(view);
        final android.app.AlertDialog dialog = builder.create();
        view.findViewById(R.id.btn_cerrar_sedes).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Clase interna para aplicar fuente personalizada en SpannableString
    public class CustomTypefaceSpan extends MetricAffectingSpan {
        private final Typeface typeface;

        public CustomTypefaceSpan(Typeface tf) {
            typeface = tf;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            apply(ds);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            apply(paint);
        }

        private void apply(TextPaint paint) {
            paint.setTypeface(typeface);
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
}
