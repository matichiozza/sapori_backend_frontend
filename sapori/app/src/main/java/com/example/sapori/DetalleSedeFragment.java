package com.example.sapori;

import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleSedeFragment extends Fragment {

    private ImageView btnCerrar, imagenSede;
    private TextView textTitulo, nombreSede, textDireccion, textTelefono, textCursos;
    private LinearLayout containerCursos;

    public DetalleSedeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detalle_sede, container, false);

        btnCerrar = view.findViewById(R.id.btn_cerrar);
        textTitulo = view.findViewById(R.id.text_filtros);
        nombreSede = view.findViewById(R.id.nombresede);
        imagenSede = view.findViewById(R.id.imagen_sede);
        textDireccion = view.findViewById(R.id.text_direccion);
        textTelefono = view.findViewById(R.id.text_telefono);
        textCursos = view.findViewById(R.id.text_Cursos);
        containerCursos = view.findViewById(R.id.containerCursos);

        btnCerrar.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                });

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("sedeId")) {
                Long sedeId = args.getLong("sedeId");
                cargarCursos(sedeId);
                cargarDatosSede(sedeId);
            }

            if (args.containsKey("urlImagenSede")) {
                String urlImagenSede = args.getString("urlImagenSede");
                Log.d("DetalleSedeFragment", "URL imagen sede: " + urlImagenSede);

                if (urlImagenSede != null && !urlImagenSede.isEmpty()) {
                    Log.d("DetalleSedeFragment", "Intentando cargar imagen con URL: " + urlImagenSede);

                    Glide.with(requireContext())
                            .load(urlImagenSede)
                            .centerCrop()
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("Glide", "❌ Falló la carga de la imagen con URL: " + urlImagenSede, e);
                                    if (e != null) {
                                        for (Throwable cause : e.getRootCauses()) {
                                            Log.e("Glide", "Causa raíz: " + cause.getMessage());
                                        }
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("Glide", "✅ Imagen cargada correctamente desde: " + model.toString());
                                    return false;
                                }
                            })
                            .into(imagenSede);
                } else {
                    Log.w("DetalleSedeFragment", "⚠️ La URL de imagen está vacía o es nula");
                }
            }
        }

        return view;
    }

    private void cargarDatosSede(Long sedeId) {
        Log.d("DetalleSedeFragment", "🔍 Buscando datos para sede ID: " + sedeId);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.obtenerSedesParaTarjetas();

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DetalleSedeFragment", "✅ Datos de sedes recibidos. Total: " + response.body().size());
                    for (Map<String, Object> sede : response.body()) {
                        Number id = (Number) sede.get("id");
                        Log.d("DetalleSedeFragment", "🔎 Revisando sede con ID: " + id);
                        if (id != null && id.longValue() == sedeId) {
                            String nombre = (String) sede.get("nombre");
                            String direccion = (String) sede.get("direccion");
                            String telefono = (String) sede.get("telefono");

                            String urlImagen = null;
                            Object fotoPrincipalObj = sede.get("fotoPrincipal");
                            if (fotoPrincipalObj instanceof String) {
                                urlImagen = (String) fotoPrincipalObj;
                                Log.d("DetalleSedeFragment", "URL imagen sede encontrada: " + urlImagen);
                            }

                            Log.d("DetalleSedeFragment", "🎯 Sede encontrada: " + nombre);

                            nombreSede.setText(nombre != null ? nombre : "Nombre no disponible");
                            textDireccion.setText("Dirección: " + (direccion != null ? direccion : "No disponible"));
                            textTelefono.setText("Teléfono: " + (telefono != null ? telefono : "No disponible"));

                            if (urlImagen != null && !urlImagen.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(urlImagen.replace(" ", "%20"))
                                        .placeholder(R.drawable.imagen_default)
                                        .error(R.drawable.imagen_default)
                                        .into(imagenSede);
                            } else {
                                imagenSede.setImageResource(R.drawable.imagen_default);
                            }

                            break;
                        }
                    }
                } else {
                    Log.w("DetalleSedeFragment", "⚠️ Error en la respuesta al obtener sedes");
                    Toast.makeText(requireContext(), "No se pudo obtener la sede", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("DetalleSedeFragment", "❌ Error al obtener sede: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error al obtener sede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCursos(Long sedeId) {
        Log.d("DetalleSedeFragment", "🔄 Cargando cursos para sede ID: " + sedeId);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.getCursosPorSede(sedeId);
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonCursos = new Gson().toJson(response.body());
                    Log.d("DetalleSedeFragment", "JSON cursos completo: " + jsonCursos);

                    Log.d("DetalleSedeFragment", "✅ Cursos recibidos: " + response.body().size());
                    mostrarCursos(response.body());
                } else {
                    Log.w("DetalleSedeFragment", "⚠️ No se encontraron cursos para esta sede");
                    Toast.makeText(requireContext(),
                            "No se encontraron cursos para esta sede", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("DetalleSedeFragment", "❌ Error al cargar cursos: " + t.getMessage(), t);
                Toast.makeText(requireContext(),
                        "Error al cargar cursos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class CustomTypefaceSpan extends TypefaceSpan {
        private final Typeface newType;

        public CustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newType = type;
        }

        @Override
        public void updateDrawState(android.text.TextPaint ds) {
            applyCustomTypeFace(ds, newType);
        }

        @Override
        public void updateMeasureState(android.text.TextPaint paint) {
            applyCustomTypeFace(paint, newType);
        }

        private static void applyCustomTypeFace(android.text.TextPaint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            oldStyle = (old == null) ? 0 : old.getStyle();

            int fake = oldStyle & ~tf.getStyle();

            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(tf);
        }
    }

    private void mostrarCursos(List<Map<String, Object>> cursos) {
        if (containerCursos == null) return;

        containerCursos.removeAllViews();

        Typeface fuenteNormal = ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium);
        Typeface fuenteTitulo = ResourcesCompat.getFont(requireContext(), R.font.poppinsbold);

        for (Map<String, Object> curso : cursos) {
            FrameLayout contenedorCurso = new FrameLayout(getContext());
            LinearLayout.LayoutParams contenedorParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            contenedorParams.setMargins(0, 0, 0, 16);
            contenedorCurso.setLayoutParams(contenedorParams);
            contenedorCurso.setBackgroundResource(R.drawable.rectangulo_curso_fondo);
            contenedorCurso.setPadding(24, 24, 24, 24);

            TextView tvCurso = new TextView(getContext());
            tvCurso.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvCurso.setTextColor(Color.parseColor("#9D9D9D"));
            tvCurso.setTypeface(fuenteNormal);

            String nombre = (String) curso.get("nombre");
            String horario = (String) curso.get("horario");
            Object vacantesDisponiblesObj = curso.get("vacantesDisponibles");
            Object precioObj = curso.get("precio");
            Object descuentoObj = curso.get("descuento");
            Object hayDescuentoObj = curso.get("hay_descuento");

            Log.d("DetalleSedeFragment", "descuento recibido: " + descuentoObj +
                    " tipo: " + (descuentoObj != null ? descuentoObj.getClass().getSimpleName() : "null"));
            Log.d("DetalleSedeFragment", "hay_descuento recibido: " + hayDescuentoObj +
                    " tipo: " + (hayDescuentoObj != null ? hayDescuentoObj.getClass().getSimpleName() : "null"));

            String diasTexto = (horario != null) ? horario : "Días no informados";

            String vacantes;
            if (vacantesDisponiblesObj instanceof Number) {
                vacantes = ((Number) vacantesDisponiblesObj).intValue() + " vacantes";
            } else {
                vacantes = "Sin info de vacantes";
            }

            String precio = "Precio no disponible";
            if (precioObj instanceof Number) {
                int valor = ((Number) precioObj).intValue();
                precio = "$" + String.format("%,d", valor).replace(',', '.');
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder();

            int start = ssb.length();
            ssb.append(nombre + "\n");
            ssb.setSpan(new CustomTypefaceSpan("", fuenteTitulo), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            String diasTitulo = "Días de cursada: ";
            start = ssb.length();
            ssb.append(diasTitulo);
            ssb.setSpan(new CustomTypefaceSpan("", fuenteTitulo), start, start + diasTitulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(diasTexto + "\n");

            String precioTitulo = "Precio: ";
            start = ssb.length();
            ssb.append(precioTitulo);
            ssb.setSpan(new CustomTypefaceSpan("", fuenteTitulo), start, start + precioTitulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(precio + "\n");

            String vacantesTitulo = "Vacantes: ";
            start = ssb.length();
            ssb.append(vacantesTitulo);
            ssb.setSpan(new CustomTypefaceSpan("", fuenteTitulo), start, start + vacantesTitulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(vacantes);

            tvCurso.setText(ssb);

            FrameLayout.LayoutParams tvCursoParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            tvCursoParams.setMargins(0, 0, 0, 0);
            tvCurso.setLayoutParams(tvCursoParams);

            contenedorCurso.addView(tvCurso);

            // Ajuste para mostrar texto de descuento considerando ambos campos
            String descuentoTexto = "";

            if (descuentoObj instanceof Number) {
                double val = ((Number) descuentoObj).doubleValue();
                if (val > 0) {
                    if (val == (int) val) {
                        descuentoTexto = String.format("%d%% OFF", (int) val);
                    } else {
                        descuentoTexto = String.format("%.1f%% OFF", val);
                    }
                }
            } else if (hayDescuentoObj instanceof Number) {
                int val = ((Number) hayDescuentoObj).intValue();
                if (val == 1) {
                    descuentoTexto = "DESCUENTO";
                }
            } else if (descuentoObj instanceof Boolean) {
                if ((Boolean) descuentoObj) descuentoTexto = "DESCUENTO";
            } else if (descuentoObj instanceof String) {
                descuentoTexto = ((String) descuentoObj).trim();
            }

            Log.d("DetalleSedeFragment", "Texto descuento calculado: " + descuentoTexto);

            if (!descuentoTexto.isEmpty()) {
                TextView tvDescuento = new TextView(getContext());
                tvDescuento.setText(descuentoTexto.toUpperCase());
                tvDescuento.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                tvDescuento.setTextColor(Color.BLACK);
                tvDescuento.setTypeface(fuenteTitulo);
                tvDescuento.setBackgroundColor(Color.parseColor("#EBD188"));
                tvDescuento.setPadding(12, 38, 12, 6);

                FrameLayout.LayoutParams descuentoParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                descuentoParams.gravity = Gravity.TOP | Gravity.END;

                // Subido un poco más (antes -22, ahora -40)
                descuentoParams.setMargins(0, -40, 0, 0);

                tvDescuento.setLayoutParams(descuentoParams);

                contenedorCurso.addView(tvDescuento);
            }

            containerCursos.addView(contenedorCurso);
        }
    }
}