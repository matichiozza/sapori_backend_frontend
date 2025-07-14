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
import com.example.sapori.model.BajaCursoRequest;
import com.example.sapori.model.Curso;
import com.example.sapori.model.PracticaCurso;
import com.example.sapori.model.PracticaCursoDTO;
import com.example.sapori.model.SedeVacanteResponse;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCursoFragment2 extends Fragment {
    private ViewPager2 viewPagerImagenes;
    private boolean desdeGestionar = false;
    private TextView txtTitulo, btnSedesDispoTexto, modalidad, modalidad2, modalidad3;
    private LinearLayout layoutDots;
    private TextView txtObjetivo, txtObjetivo1;
    private TextView txtHorario, txtFechaInicio, txtSede, btnDebajo;
    private ImageView logoPresencial, logoNuevo;
    private ImageView flecha;
    private Curso curso;
    private ImageView puntoAmarillo, puntoRojo, puntoVerde;
    private TextView txtProximamente, txtFinalizado, txtEnCurso;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_curso2, container, false);

        if (getArguments() != null) {
            desdeGestionar = getArguments().getBoolean("desdeGestionar", false);
        }

        layoutDots = view.findViewById(R.id.indicatorLayout);
        viewPagerImagenes = view.findViewById(R.id.viewPagerImagenes);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        btnSedesDispoTexto = view.findViewById(R.id.btnSedesDispoTexto);
        logoPresencial = view.findViewById(R.id.logo_presencial);
        logoNuevo = view.findViewById(R.id.logo_nuevo);
        modalidad = view.findViewById(R.id.modalidad);
        modalidad2 = view.findViewById(R.id.modalidad2);
        modalidad3 = view.findViewById(R.id.modalidad3);
        txtObjetivo = view.findViewById(R.id.txtObjetivo);
        txtObjetivo1 = view.findViewById(R.id.txtObjetivo1);
        puntoAmarillo = view.findViewById(R.id.punto_amarillo);
        puntoRojo = view.findViewById(R.id.punto_rojo);
        puntoVerde = view.findViewById(R.id.punto_verde);
        txtHorario = view.findViewById(R.id.txtHorario);
        txtFechaInicio = view.findViewById(R.id.txtFechaInicio);
        txtSede = view.findViewById(R.id.txtSede);
        btnDebajo = view.findViewById(R.id.btnDebajo);

        txtProximamente = view.findViewById(R.id.txt_en_curso1);
        txtFinalizado = view.findViewById(R.id.txt_en_curso2);
        txtEnCurso = view.findViewById(R.id.txt_en_curso);

        flecha = view.findViewById(R.id.btnAtras);
        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        Bundle args = getArguments();
        Log.d("DetalleCurso", "Args recibidos: " + args);

        Long cursoId;
        if (args != null && args.containsKey("cursoId")) {
            cursoId = args.getLong("cursoId");
            Log.d("DetalleCurso", "cursoId recibido: " + cursoId);
        } else {
            cursoId = null;
            Log.e("DetalleCurso", "NO SE RECIBIÓ cursoId en el Bundle");
        }

        if (cursoId != null && cursoId != 0) {
            apiService = ApiClient.getApiService();
            obtenerCursoDesdeBackend(cursoId);
        } else if (args != null && args.containsKey("nombre")) {
            // Datos recibidos directamente sin backend
            String nombre = args.getString("nombre", "-");
            String modalidadStr = args.getString("modalidad", "-");
            String imagenUrl = args.getString("imagenUrl", null);
            String estadoCurso = args.getString("estadoCurso", "");
            String horario = args.getString("horario", "");
            String sede = args.getString("sede", "");
            String fechaInicio = args.getString("fechaInicio", "");
            
            txtTitulo.setText(nombre != null ? nombre : "-");
            modalidad.setText(modalidadStr);
            modalidad2.setText(modalidadStr);
            txtHorario.setText(horario != null ? horario : "-");
            
            // Mostrar fecha de inicio si está disponible
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                String fechaFormateada = formatearFecha(fechaInicio);
                txtFechaInicio.setText(fechaFormateada);
            } else {
                txtFechaInicio.setText("No disponible");
            }
            
            // Mostrar sede si está disponible
            if (sede != null && !sede.isEmpty()) {
                txtSede.setText(sede);
            } else {
                txtSede.setText("No especificada");
            }

            if ("presencial".equalsIgnoreCase(modalidadStr)) {
                logoPresencial.setVisibility(View.VISIBLE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
            } else if ("virtual".equalsIgnoreCase(modalidadStr)) {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.VISIBLE);
                modalidad3.setVisibility(View.VISIBLE);
            } else {
                logoPresencial.setVisibility(View.GONE);
                logoNuevo.setVisibility(View.GONE);
                modalidad2.setVisibility(View.GONE);
            }

            // Crear lista fotos para el adapter (evitar nullpointer)
            List<String> fotos = new ArrayList<>();
            if (imagenUrl != null) {
                fotos.add(imagenUrl);
            }
            ImagenURLAdapter imagenAdapter = new ImagenURLAdapter(fotos);
            viewPagerImagenes.setAdapter(imagenAdapter);
            agregarIndicadoresDots(fotos.size(), 0);

            viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    agregarIndicadoresDots(fotos.size(), position);
                }
            });

        } else {
            Toast.makeText(requireContext(), "No se recibió información del curso", Toast.LENGTH_SHORT).show();
            Log.e("DetalleCurso", "No se recibió cursoId ni datos del curso");
        }

        btnSedesDispoTexto.setOnClickListener(v -> {
            if (curso != null && curso.getId() != null) {
                Bundle bundle = new Bundle();
                bundle.putLong("cursoId", curso.getId());
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.fragment_detalle_curso, bundle);
            } else {
                Toast.makeText(getContext(), "Curso no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Navegación a asistencia
        TextView btnAsistencia = view.findViewById(R.id.btnOtro);
        btnAsistencia.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle bundle = new Bundle();
            bundle.putLong("cursoId", cursoId);
            navController.navigate(R.id.action_detalleCursoFragment2_to_asistenciaFragment, bundle);
        });

        // Navegación a materiales de clase
        TextView btnMateriales = view.findViewById(R.id.btnMateriales);
        Log.d("DEBUG", "btnMateriales es null? " + (btnMateriales == null));
        btnMateriales.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle bundle = new Bundle();
            bundle.putLong("cursoId", cursoId);
            navController.navigate(R.id.action_detalleCursoFragment2_to_materialesClaseFragment, bundle);
        });

        btnDebajo.setOnClickListener(v -> {
            PopupCursoFragment popup = new PopupCursoFragment();

            popup.setPopupListener(new PopupCursoFragment.PopupListener() {
                @Override
                public void onConfirm() {
                    // Calcular reintegro según la fecha de inicio
                    calcularReintegroYMostrarOpciones();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getContext(), "Cancelado: No dar de baja", Toast.LENGTH_SHORT).show();
                }
            });

            popup.show(getParentFragmentManager(), "popup_darme_baja");
        });

        return view;
    }

    private void actualizarEstadoCurso(String estadoCurso) {
        // Primero ocultar todo
        puntoAmarillo.setVisibility(View.GONE);
        puntoRojo.setVisibility(View.GONE);
        puntoVerde.setVisibility(View.GONE);

        txtProximamente.setVisibility(View.GONE);
        txtFinalizado.setVisibility(View.GONE);
        txtEnCurso.setVisibility(View.GONE);

        if (estadoCurso == null) return;

        switch (estadoCurso.toLowerCase()) {
            case "proximamente":
            case "proximado":  // si hay variaciones
                puntoAmarillo.setVisibility(View.VISIBLE);
                txtProximamente.setVisibility(View.VISIBLE);
                break;
            case "finalizado":
                puntoRojo.setVisibility(View.VISIBLE);
                txtFinalizado.setVisibility(View.VISIBLE);
                break;
            case "en_curso":
            case "encurso":
                puntoVerde.setVisibility(View.VISIBLE);
                txtEnCurso.setVisibility(View.VISIBLE);
                break;
            default:
                // ocultar todo o manejar otros casos
                break;
        }
    }

    private void obtenerCursoDesdeBackend(Long cursoId) {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long alumnoId = userPrefs.getLong("id_usuario", -1);
        
        Call<Map<String, Object>> call = apiService.obtenerCursoConHorarios(cursoId, alumnoId != -1 ? alumnoId : null);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> cursoData = response.body();
                    
                    // Crear objeto Curso desde los datos del mapa
                    curso = new Curso();
                    curso.setId(((Number) cursoData.get("id")).longValue());
                    curso.setNombre((String) cursoData.get("nombre"));
                    curso.setFotosUrl((List<String>) cursoData.get("fotosUrl"));
                    curso.setModalidad((String) cursoData.get("modalidad"));
                    curso.setImporte(((Number) cursoData.get("importe")).floatValue());
                    curso.setDuracion(((Number) cursoData.get("duracion")).intValue());
                    curso.setObjetivo((String) cursoData.get("objetivo"));
                    curso.setFechaInicio((String) cursoData.get("fechaInicio"));
                    curso.setFechaFin((String) cursoData.get("fechaFin"));
                    curso.setEstadoCurso((String) cursoData.get("estadoCurso"));
                    curso.setDescripcion((String) cursoData.get("descripcion"));
                    curso.setTemario((String) cursoData.get("temario"));
                    curso.setRequisitos((String) cursoData.get("requisitos"));
                    
                    // Guardar los datos de horarios para usarlos en cargarDatosEnVista
                    List<Map<String, Object>> sedesConHorarios = (List<Map<String, Object>>) cursoData.get("sedesConHorarios");
                    
                    // Obtener información de inscripción
                    Boolean estaInscripto = (Boolean) cursoData.get("estaInscripto");
                    String sedeInscripto = (String) cursoData.get("sedeInscripto");
                    String horarioInscripto = (String) cursoData.get("horarioInscripto");
                    
                    Log.d("DetalleCursoFragment", "Datos recibidos del backend:");
                    Log.d("DetalleCursoFragment", "estaInscripto: " + estaInscripto);
                    Log.d("DetalleCursoFragment", "sedeInscripto: " + sedeInscripto);
                    Log.d("DetalleCursoFragment", "horarioInscripto: " + horarioInscripto);
                    Log.d("DetalleCursoFragment", "fechaInicio: " + curso.getFechaInicio());
                    
                    cargarDatosEnVista(sedesConHorarios, estaInscripto, sedeInscripto, horarioInscripto);

                    if (alumnoId == -1) {
                        Toast.makeText(requireContext(), "Usuario no identificado", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e("API", "Error en respuesta: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar curso", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
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
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics())
        );
        params.setMargins(0, 24, 0, 24);
        linea.setLayoutParams(params);
        linea.setBackgroundColor(Color.parseColor("#D3D3D3"));
        return linea;
    }

    private void cargarDatosEnVista() {
        cargarDatosEnVista(null, null, null, null);
    }

    private void cargarDatosEnVista(List<Map<String, Object>> sedesConHorarios) {
        cargarDatosEnVista(sedesConHorarios, null, null, null);
    }

    private void cargarDatosEnVista(List<Map<String, Object>> sedesConHorarios, Boolean estaInscripto, String sedeInscripto, String horarioInscripto) {
        Log.d("DetalleCursoFragment", "Iniciando carga de datos en vista");

        txtTitulo.setText(curso.getNombre());
        Log.d("DetalleCursoFragment", "Título: " + curso.getNombre());

        // Mostrar fecha de inicio del curso
        if (curso.getFechaInicio() != null && !curso.getFechaInicio().isEmpty()) {
            String fechaFormateada = formatearFecha(curso.getFechaInicio());
            txtFechaInicio.setText(fechaFormateada);
            Log.d("DETALLE_CURSO", "Fecha de inicio recibida: " + curso.getFechaInicio() + " | Formateada: " + fechaFormateada);
        } else {
            txtFechaInicio.setText("No disponible");
            Log.d("DETALLE_CURSO", "Fecha de inicio recibida: null o vacía");
        }

        // Mostrar sede donde está cursando (si está inscripto)
        if (estaInscripto != null && estaInscripto && sedeInscripto != null) {
            txtSede.setText(sedeInscripto);
            Log.d("DETALLE_CURSO", "Sede inscripto recibida: " + sedeInscripto);
        } else {
            txtSede.setText("No inscripto");
            Log.d("DETALLE_CURSO", "Sede inscripto recibida: null o no inscripto");
        }

        // Obtener el horario de la cursada
        if (estaInscripto != null && estaInscripto && horarioInscripto != null) {
            // Si está inscripto, mostrar el horario de su sede
            txtHorario.setText(horarioInscripto);
            Log.d("DetalleCursoFragment", "Horario inscripto: " + horarioInscripto);
        } else if (sedesConHorarios != null && !sedesConHorarios.isEmpty()) {
            // Si no está inscripto, mostrar el primer horario disponible
            String horario = obtenerHorarioDesdeSedes(sedesConHorarios);
            txtHorario.setText(horario != null ? horario : "-");
            Log.d("DetalleCursoFragment", "Horario obtenido desde sedes: " + horario);
        } else {
            txtHorario.setText("-");
            Log.d("DetalleCursoFragment", "No hay horarios disponibles");
        }

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

        ((ViewGroup) txtObjetivo1.getParent()).addView(crearLineaDivisoria(),
                ((ViewGroup) txtObjetivo1.getParent()).indexOfChild(txtObjetivo1) + 1);

        String tituloUtensilios = "Utensillos:";
        String utensilios = curso.getRequisitos() != null ? curso.getRequisitos().trim() : "";
        String textoCompleto = tituloUtensilios + " " + utensilios;

        SpannableString spannableUtensilios = new SpannableString(textoCompleto);
        spannableUtensilios.setSpan(new CustomTypefaceSpan(fuenteUtensiliosTitulo), 0, tituloUtensilios.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), 0, tituloUtensilios.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new CustomTypefaceSpan(fuenteUtensiliosContenido), tituloUtensilios.length() + 1, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableUtensilios.setSpan(new ForegroundColorSpan(Color.parseColor("#9D9D9D")), tituloUtensilios.length() + 1, textoCompleto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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

        actualizarEstadoCurso(curso.getEstadoCurso());

        agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, 0);

        viewPagerImagenes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                agregarIndicadoresDots(curso.getFotosUrl() != null ? curso.getFotosUrl().size() : 0, position);
            }
        });

        Log.d("DetalleCursoFragment", "Carga de datos completada");
    }

    private String formatearFecha(String fechaISO) {
        try {
            // Asumiendo que la fecha viene en formato ISO 8601 (ej: "2024-06-03T13:45:00")
            String[] partes = fechaISO.split("T")[0].split("-");
            if (partes.length >= 3) {
                String dia = partes[2];
                String mes = partes[1];
                String año = partes[0];
                return dia + "/" + mes + "/" + año;
            }
        } catch (Exception e) {
            Log.e("DetalleCursoFragment", "Error formateando fecha: " + e.getMessage());
        }
        return fechaISO; // Devolver la fecha original si no se puede formatear
    }

    private String obtenerHorarioDesdeSedes(List<Map<String, Object>> sedesConHorarios) {
        // Buscar el horario en las sedes disponibles
        for (Map<String, Object> sede : sedesConHorarios) {
            String horario = (String) sede.get("cursadaHorarioDia");
            if (horario != null && !horario.isEmpty()) {
                return horario;
            }
        }
        return null;
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < sedes.size() - 1) {
                params.setMargins(0, 0, 0, 32);
            }
            sedeView.setLayoutParams(params);

            layoutSedes.addView(sedeView);
        }

        builder.setView(view);
        final android.app.AlertDialog dialog = builder.create();
        view.findViewById(R.id.btn_cerrar_sedes).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

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

    private void calcularReintegroYMostrarOpciones() {
        if (curso == null || curso.getFechaInicio() == null || curso.getImporte() == null) {
            Toast.makeText(getContext(), "Error: No se puede calcular el reintegro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular días hábiles hasta el inicio del curso
        int diasHabiles = calcularDiasHabilesHastaInicio(curso.getFechaInicio());
        float montoReintegro;
        int porcentajeReintegro;

        // Aplicar reglas de reintegro según la consigna
        if (diasHabiles >= 10) {
            // Hasta 10 días antes: Reintegro total
            montoReintegro = curso.getImporte();
            porcentajeReintegro = 100;
        } else if (diasHabiles >= 1) {
            // De 9 a 1 día antes: Reintegro 70%
            montoReintegro = curso.getImporte() * 0.70f;
            porcentajeReintegro = 70;
        } else if (diasHabiles == 0) {
            // Día de inicio: Reintegro 50%
            montoReintegro = curso.getImporte() * 0.50f;
            porcentajeReintegro = 50;
        } else {
            // Después de iniciar: No hay reintegro
            montoReintegro = 0;
            porcentajeReintegro = 0;
        }

        // Mostrar popup de opciones de reintegro
        PopupOpcionesReintegroFragment popupOpciones = new PopupOpcionesReintegroFragment();
        popupOpciones.setDatosReintegro(montoReintegro, porcentajeReintegro);
        
        popupOpciones.setOpcionesReintegroListener(new PopupOpcionesReintegroFragment.OpcionesReintegroListener() {
            @Override
            public void onReintegroTarjeta() {
                procesarBaja("TARJETA", montoReintegro, porcentajeReintegro);
            }

            @Override
            public void onSaldoFavor() {
                procesarBaja("SALDO_FAVOR", montoReintegro, porcentajeReintegro);
            }

            @Override
            public void onCancelar() {
                Toast.makeText(getContext(), "Operación cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        popupOpciones.show(getParentFragmentManager(), "popup_opciones_reintegro");
    }

    private int calcularDiasHabilesHastaInicio(String fechaInicio) {
        try {
            // Parsear la fecha de inicio (formato ISO)
            java.time.LocalDateTime fechaInicioCurso = java.time.LocalDateTime.parse(fechaInicio);
            java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
            
            // Calcular días entre fechas
            long diasTotales = java.time.Duration.between(fechaActual, fechaInicioCurso).toDays();
            
            // Simplificación: asumimos que todos los días son hábiles
            // En una implementación real, habría que excluir fines de semana y feriados
            return (int) diasTotales;
        } catch (Exception e) {
            Log.e("DetalleCursoFragment2", "Error calculando días hábiles: " + e.getMessage());
            return -1;
        }
    }

    private void procesarBaja(String tipoReintegro, float montoReintegro, int porcentajeReintegro) {
        SharedPreferences userPrefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = userPrefs.getLong("id_usuario", -1);
        long cuentaCorrienteId = userPrefs.getLong("cuenta_corriente_id", -1);

        Log.d("BajaCurso", "usuarioId: " + usuarioId + ", cuentaCorrienteId (SharedPrefs): " + cuentaCorrienteId);

        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cuentaCorrienteId == -1) {
            // Si no está en SharedPreferences, consulta la API
            ApiService apiService = ApiClient.getApiService();
            Log.d("BajaCurso", "Consultando cuenta corriente para usuarioId: " + usuarioId);
            apiService.obtenerCuentaCorriente(usuarioId).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    Log.d("BajaCurso", "Respuesta obtenerCuentaCorriente: " + response);
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("BajaCurso", "Body: " + response.body().toString());
                        Log.d("BajaCurso", "Claves en body: " + response.body().keySet());
                    }
                    if (response.isSuccessful() && response.body() != null && response.body().containsKey("cuentaCorrienteId")) {
                        Object idObj = response.body().get("cuentaCorrienteId");
                        Log.d("BajaCurso", "Valor crudo cuentaCorrienteId: " + idObj);
                        Long cuentaCorrienteIdApi = null;
                        if (idObj instanceof Number) {
                            cuentaCorrienteIdApi = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            try {
                                cuentaCorrienteIdApi = Long.parseLong((String) idObj);
                            } catch (NumberFormatException ignored) {}
                        }
                        Log.d("BajaCurso", "cuentaCorrienteId parseado: " + cuentaCorrienteIdApi);
                        if (cuentaCorrienteIdApi != null) {
                            // Guardar en SharedPreferences para futuras bajas
                            userPrefs.edit().putLong("cuenta_corriente_id", cuentaCorrienteIdApi).apply();
                            ejecutarBaja(usuarioId, cuentaCorrienteIdApi, tipoReintegro, montoReintegro, porcentajeReintegro);
                        } else {
                            Log.e("BajaCurso", "No se pudo parsear cuentaCorrienteId");
                            Toast.makeText(requireContext(), "No se pudo obtener la cuenta corriente", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("BajaCurso", "No se encontró cuentaCorrienteId en la respuesta: " + (response.body() != null ? response.body().toString() : "null"));
                        Toast.makeText(requireContext(), "No se pudo obtener la cuenta corriente", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("BajaCurso", "Error de conexión al buscar cuenta corriente", t);
                    Toast.makeText(requireContext(), "Error de conexión al buscar cuenta corriente", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Si ya está en SharedPreferences, ejecuta la baja normalmente
        ejecutarBaja(usuarioId, cuentaCorrienteId, tipoReintegro, montoReintegro, porcentajeReintegro);
    }

    private void ejecutarBaja(long usuarioId, long cuentaCorrienteId, String tipoReintegro, float montoReintegro, int porcentajeReintegro) {
        BajaCursoRequest request = new BajaCursoRequest(usuarioId, curso.getId(), tipoReintegro, montoReintegro, porcentajeReintegro, cuentaCorrienteId);
        ApiService apiService = ApiClient.getApiService();
        Call<Map<String, Object>> call = apiService.darBajaDeCurso(request);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String mensaje = "Baja procesada exitosamente";
                    if (tipoReintegro.equals("TARJETA")) {
                        mensaje += ". Se procesará el reintegro a tu tarjeta.";
                    } else {
                        mensaje += ". El saldo se agregó a tu cuenta corriente.";
                    }
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    String errorMessage = "Error al procesar la baja";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage = "Error al procesar la baja";
                        }
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}