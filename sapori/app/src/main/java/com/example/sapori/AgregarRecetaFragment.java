package com.example.sapori;

import static android.app.Activity.RESULT_OK;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteDTO;
import com.example.sapori.model.IngredienteReceta;
import com.example.sapori.model.PasoReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarRecetaFragment extends Fragment {

    private LinearLayout containerPasos;
    private int contadorPasos = 1;
    private List<Ingrediente> ingredientes;
    private EditText editTitulo, editDescripcion, editPreparacion;
    private final List<Uri> imagenesSeleccionadas = new ArrayList<>();
    private LinearLayout btnAgregarFoto, btnSeleccionarTipo, containerIngredientes;
    private List<IngredienteReceta> ingredientesSeleccionados = new ArrayList<>();
    private ImageView flecha;
    private int posicionIngredienteReemplazar = -1;
    private String nombreTipoPlatoSeleccionado;
    private ImageView imageViewSeleccionado;
    private TextView textViewSeleccionado, btnPublicar;

    private TextView tipoPlatoSeleccionado;
    private List<PasoReceta> pasosSeleccionados = new ArrayList<>();

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

    private Long recetaId = null; // <-- Declaración de recetaId

    public AgregarRecetaFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener recetaId de argumentos (si viene)
        if (getArguments() != null) {
            recetaId = getArguments().getLong("recetaId", -1);
            if (recetaId == -1) {
                recetaId = null; // No válido
            }
        }
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
        Log.d("AgregarRecetaFragment", "Ingredientes seleccionados limpiados");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_agregar_receta, container, false);

        containerPasos = view.findViewById(R.id.container_pasos);
        LinearLayout layoutAgregarPaso = view.findViewById(R.id.layout_agregar_paso);

        editTitulo = view.findViewById(R.id.edit_titulo);
        editDescripcion = view.findViewById(R.id.edit_descripcion);
        btnPublicar = view.findViewById(R.id.btn_publicar_receta);

        editPreparacion = view.findViewById(R.id.edit_preparacion);

        btnAgregarFoto = view.findViewById(R.id.btn_agregar_foto);
        btnSeleccionarTipo = view.findViewById(R.id.btn_tipo_plato);
        containerIngredientes = view.findViewById(R.id.container_ingredientes);
        flecha = view.findViewById(R.id.flecha);

        editTitulo.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));
        editDescripcion.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));
        editPreparacion.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));

        flecha.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        // Agregar 5 contenedores con borde punteado y un ImageView + TextView adentro
        for (int i = 0; i < 5; i++) {
            FrameLayout frame = createImageFrame(requireContext());
            btnAgregarFoto.addView(frame);
        }

        // Agregar 4 contenedores para ingredientes con borde punteado y un EditText + TextView adentro
        for (int i = 0; i < 4; i++) {
            FrameLayout frameIngrediente = createIngredienteFrame(requireContext());
            containerIngredientes.addView(frameIngrediente);
        }

        FrameLayout addFrame = createAddIngredienteFrame(requireContext());
        containerIngredientes.addView(addFrame);

        // Agregar tipos de plato al container btnSeleccionarTipo
        crearTiposDePlato(requireContext());

        getParentFragmentManager().setFragmentResultListener("receta_agregada", this, (key, bundle) -> {
            // Aquí manejar cuando se publica una receta
        });

        // --- Funcionalidad de sumar y restar ---
        TextView tvCantidad = view.findViewById(R.id.tvCantidad);
        View btnSumar = view.findViewById(R.id.btnSumar);
        View btnRestar = view.findViewById(R.id.btnRestar);

        btnSumar.setOnClickListener(v -> {
            int cantidad = Integer.parseInt(tvCantidad.getText().toString());
            cantidad++;
            tvCantidad.setText(String.valueOf(cantidad));
        });

        btnRestar.setOnClickListener(v -> {
            int cantidad = Integer.parseInt(tvCantidad.getText().toString());
            if (cantidad > 1) {
                cantidad--;
                tvCantidad.setText(String.valueOf(cantidad));
            }
        });

        layoutAgregarPaso.setOnClickListener(v -> {
            contadorPasos++;

            EditText nuevoPaso = new EditText(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(150) // igual que el original
            );
            params.topMargin = dpToPx(10);
            nuevoPaso.setLayoutParams(params);
            nuevoPaso.setHint("Paso " + contadorPasos + "...");
            nuevoPaso.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_bg_rounded_7));
            nuevoPaso.setPadding(dpToPx(15), dpToPx(9), dpToPx(16), 0);
            nuevoPaso.setTextSize(17);
            nuevoPaso.setTextColor(Color.parseColor("#808080"));
            nuevoPaso.setHintTextColor(Color.parseColor("#808080"));
            nuevoPaso.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));
            nuevoPaso.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            nuevoPaso.setGravity(Gravity.TOP | Gravity.START);

            containerPasos.addView(nuevoPaso);
        });

        btnPublicar.setOnClickListener(v -> {
            publicarReceta();
        });

        return view;
    }





    private void publicarReceta() {
        // Limpiar la lista de pasos antes de llenarla
        pasosSeleccionados.clear();

        // Recorrer los EditText agregados en containerPasos para extraer la descripción de cada paso
        for (int i = 0; i < containerPasos.getChildCount(); i++) {
            View child = containerPasos.getChildAt(i);
            if (child instanceof EditText) {
                String textoPaso = ((EditText) child).getText().toString().trim();
                if (!textoPaso.isEmpty()) {
                    PasoReceta paso = new PasoReceta();
                    paso.setNumeroPaso(i + 1);
                    paso.setDescripcion(textoPaso);
                    paso.setMediaUrls(new ArrayList<>());
                    pasosSeleccionados.add(paso);
                }
            }
        }

        // El resto del método queda igual, sólo que ahora receta tiene la lista de pasos actualizada
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String tiempoStr = editPreparacion.getText().toString().trim();

        TextView tvCantidad = getView().findViewById(R.id.tvCantidad);
        int porciones;
        try {
            porciones = Integer.parseInt(tvCantidad.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Cantidad de porciones inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        if (titulo.isEmpty()) {
            Toast.makeText(requireContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imagenesSeleccionadas.isEmpty()) {
            Toast.makeText(requireContext(), "La receta debe tener al menos 1 imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer tiempoPreparacion = null;
        if (!tiempoStr.isEmpty()) {
            try {
                tiempoPreparacion = Integer.parseInt(tiempoStr);
                if (tiempoPreparacion < 0) {
                    Toast.makeText(requireContext(), "El tiempo de preparación no puede ser negativo", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "El tiempo de preparación debe ser un número válido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        String aliasUsuario = sharedPreferences.getString("nombre_usuario", null);

        if (aliasUsuario == null || aliasUsuario.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró un usuario válido para crear la receta", Toast.LENGTH_SHORT).show();
            return;
        }

        Receta receta = new Receta();
        receta.setNombre(titulo);
        receta.setDescripcion(descripcion);
        receta.setTiempo(tiempoPreparacion);
        receta.setPorciones(porciones);
        receta.setIngredientes(ingredientesSeleccionados);
        receta.setPasos(pasosSeleccionados);  // <- asegurate de tener el setter para pasos
        if (tipoPlatoSeleccionado != null) {
            receta.setTipo(tipoPlatoSeleccionado.getText().toString());
        } else {
            Toast.makeText(requireContext(), "Seleccioná un tipo de plato", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        String recetaJson = gson.toJson(receta);
        Log.d("RECETA_JSON", recetaJson); // 👈 Acá vemos exactamente qué se envía

        ApiService apiService = ApiClient.getApiService();
        apiService.crearReceta(aliasUsuario, receta).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long recetaId = response.body().getId();

                    if (recetaId == null) {
                        Toast.makeText(requireContext(), "Error: La receta creada no tiene ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    enviarIngredientesReceta(recetaId);

                    enviarPasosReceta(recetaId);

                    if (!imagenesSeleccionadas.isEmpty()) {
                        subirImagenReceta(imagenesSeleccionadas, recetaId, 0);
                    } else {
                        notificarYCerrar();
                    }
                } else {
                    Log.e("AgregarReceta", "Error al crear receta: " + response.code());
                    Toast.makeText(requireContext(), "Error al crear receta: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("AgregarReceta", "Fallo en la petición: ", t);
                Toast.makeText(requireContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirImagenReceta(List<Uri> imagenes, Long recetaId, int index) {
        if (index >= imagenes.size()) {
            // Todas las imágenes subidas, cerrar y notificar
            notificarYCerrar();
            return;
        }

        Uri uri = imagenes.get(index);

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[16384];
            int nRead;

            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("imagen", "foto.jpg", requestFile);

            ApiService api = ApiClient.getApiService();
            api.subirImagenReceta(recetaId, imagenPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d("Imagen", "Imagen " + (index + 1) + " subida con éxito");
                        // Subir siguiente imagen
                        subirImagenReceta(imagenes, recetaId, index + 1);
                    } else {
                        Log.e("Imagen", "Error en la respuesta: " + response.code());
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error al subir imagen: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                        // Puedes decidir si continúas o no con las siguientes imágenes
                        subirImagenReceta(imagenes, recetaId, index + 1);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Imagen", "Fallo al subir imagen", t);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // También puedes continuar con las siguientes imágenes si quieres
                    subirImagenReceta(imagenes, recetaId, index + 1);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Imagen", "Error leyendo la imagen", e);
            // Continuar con las siguientes imágenes en caso de error
            subirImagenReceta(imagenes, recetaId, index + 1);
        }
    }

    private void notificarYCerrar() {
        if (!isAdded()) {
            // El fragmento no está activo, no hacer nada para evitar crash
            return;
        }
        Bundle resultado = new Bundle();
        resultado.putBoolean("nueva_receta", true);
        getParentFragmentManager().setFragmentResult("receta_agregada", resultado);

        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imagenSeleccionada = data.getData();
            if (imagenSeleccionada != null) {
                imagenesSeleccionadas.add(imagenSeleccionada);

                if (imageViewSeleccionado != null && textViewSeleccionado != null) {
                    imageViewSeleccionado.setImageURI(imagenSeleccionada);
                    textViewSeleccionado.setVisibility(View.GONE);
                }
            }
        }
    }



    private FrameLayout createImageFrame(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);

        int sizeInDp = 120;
        int marginInDp = 8;
        int paddingInDp = 6;  // Espacio para que el borde quede visible

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingInDp, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);

        // Fondo con borde punteado
        frameLayout.setBackgroundResource(R.drawable.borde_imagen);

        // Crear ImageView para mostrar la imagen
        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackground(null);

        // Crear TextView centrado con texto "¡Sube aquí!"
        TextView textView = new TextView(context);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParams);
        textView.setText("¡Sube aquí!");
        textView.setTextColor(Color.parseColor("#BCBCBC")); // Cambia color si quieres
        textView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsbold));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        // Cuando se toca la imagen o el texto, abrimos selector
        View.OnClickListener abrirSelectorListener = v -> {
            imageViewSeleccionado = imageView;
            textViewSeleccionado = textView;
            abrirSelectorImagen();
        };

        imageView.setOnClickListener(abrirSelectorListener);
        textView.setOnClickListener(abrirSelectorListener);

        // Añadir el ImageView y el TextView al FrameLayout
        frameLayout.addView(imageView);
        frameLayout.addView(textView);

        return frameLayout;
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private void crearTiposDePlato(Context context) {
        btnSeleccionarTipo.removeAllViews();

        int marginInDp = 4;
        int paddingHorizontalDp = 16;
        int paddingVerticalDp = 8;

        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingHorizontalPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingHorizontalDp, context.getResources().getDisplayMetrics());
        int paddingVerticalPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingVerticalDp, context.getResources().getDisplayMetrics());

        final LinearLayout[] layoutTipoSeleccionado = {null};
        final ImageView[] iconoSeleccionado = {null};

        for (String tipo : tiposDePlato) {
            LinearLayout layoutTipo = new LinearLayout(context);
            layoutTipo.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(marginInPx, 0, marginInPx, 0);
            layoutTipo.setLayoutParams(layoutParams);
            layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_no_seleccionado);
            layoutTipo.setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx);
            layoutTipo.setClickable(true);
            layoutTipo.setGravity(Gravity.CENTER_VERTICAL);

            ImageView icono = new ImageView(context);
            int iconSizeDp = 24;
            int iconSizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, iconSizeDp, context.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            iconParams.setMargins(0, 0, marginInPx, 0);
            icono.setLayoutParams(iconParams);

            Integer resIcono = iconosPorTipo.get(tipo);
            if (resIcono != null) {
                icono.setImageResource(resIcono);
                icono.setTag(resIcono); // Guardamos el recurso original
            }

            TextView tipoView = new TextView(context);
            tipoView.setText(tipo);
            tipoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tipoView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsbold));
            tipoView.setTextColor(Color.DKGRAY);

            layoutTipo.addView(icono);
            layoutTipo.addView(tipoView);

            layoutTipo.setOnClickListener(v -> {
                if (layoutTipoSeleccionado[0] == layoutTipo) {
                    layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_no_seleccionado);

                    // Restaurar ícono original (color)
                    Object tag = icono.getTag();
                    if (tag instanceof Integer) {
                        icono.setImageResource((Integer) tag);
                    }
                    icono.setColorFilter(null); // Quitar tint blanco si lo tenía

                    tipoView.setTextColor(Color.DKGRAY);
                    tipoPlatoSeleccionado = null;
                    layoutTipoSeleccionado[0] = null;
                    iconoSeleccionado[0] = null;
                    return;
                }

                if (layoutTipoSeleccionado[0] != null) {
                    layoutTipoSeleccionado[0].setBackgroundResource(R.drawable.bg_tipo_plato_no_seleccionado);

                    if (iconoSeleccionado[0] != null) {
                        Object tag = iconoSeleccionado[0].getTag();
                        if (tag instanceof Integer) {
                            iconoSeleccionado[0].setImageResource((Integer) tag);
                        }
                        iconoSeleccionado[0].setColorFilter(null); // Quitar tint blanco anterior
                    }

                    TextView textoAnterior = (TextView) layoutTipoSeleccionado[0].getChildAt(1);
                    textoAnterior.setTextColor(Color.DKGRAY);
                }

                layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_seleccionado);
                icono.setAlpha(1f);
                icono.setColorFilter(Color.WHITE); // Pintar de blanco el icono seleccionado
                tipoView.setTextColor(Color.WHITE);

                layoutTipoSeleccionado[0] = layoutTipo;
                iconoSeleccionado[0] = icono;
                tipoPlatoSeleccionado = tipoView;
            });

            btnSeleccionarTipo.addView(layoutTipo);
        }

    }

    private FrameLayout createIngredienteFrame(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);

        int sizeInDp = 120;  // ancho y alto cuadrados
        int marginInDp = 8;
        int paddingInDp = 6;  // para que el borde quede visible

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingInDp, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, marginInPx, 0);  // margen derecho
        frameLayout.setLayoutParams(params);

        frameLayout.setBackgroundResource(R.drawable.borde_ingrediente);
        frameLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

        ImageView icono = new ImageView(context);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = Gravity.CENTER;
        icono.setLayoutParams(iconParams);
        icono.setImageResource(R.drawable.icon_add);

        // Listener para abrir el popup de ingredientes
        icono.setOnClickListener(v -> {
            showIngredientePopup(context);
        });



        frameLayout.addView(icono);

        return frameLayout;
    }

    private void showIngredientePopup(Context context) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;

            activity.getSupportFragmentManager().setFragmentResultListener(
                    "ingredientesSeleccionadosKey",
                    activity,
                    (requestKey, bundle) -> {
                        ArrayList<String> ingredientesIds = bundle.getStringArrayList("ingredientesSeleccionados");

                        if (ingredientesIds != null) {
                            Log.d("showIngredientePopup", "Ingredientes seleccionados: " + ingredientesIds);

                            ApiService apiService = ApiClient.getApiService();
                            apiService.listarIngredientes().enqueue(new Callback<List<Ingrediente>>() {
                                @Override
                                public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<Ingrediente> todos = response.body();
                                        List<Ingrediente> seleccionados = new ArrayList<>();
                                        for (Ingrediente ingrediente : todos) {
                                            if (ingredientesIds.contains(String.valueOf(ingrediente.getId()))) {
                                                seleccionados.add(ingrediente);
                                            }
                                        }
                                        mostrarIngredientesSeleccionados(seleccionados);
                                    } else {
                                        Log.e("API", "Respuesta no exitosa");
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                                    Log.e("API", "Error al obtener ingredientes", t);
                                }
                            });
                        } else {
                            Log.d("showIngredientePopup", "No llegó bundle o no contiene 'ingredientesSeleccionados'");
                        }
                    }
            );

            IngredientesFragment2 ingredientesFragment2 = new IngredientesFragment2();
            ingredientesFragment2.show(activity.getSupportFragmentManager(), "ingredientes_fragment");

        } else {
            Log.e("showIngredientePopup", "El contexto no es una AppCompatActivity, no se puede mostrar el fragment.");
        }
    }

    private FrameLayout createAddIngredienteFrame(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);

        int sizeInDp = 120;
        int marginInDp = 8;
        int paddingInDp = 6;

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingInDp, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);

        frameLayout.setBackgroundResource(R.drawable.borde_imagen);

        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.icon_add);  // Icono para "agregar"

        frameLayout.addView(imageView);

        frameLayout.setOnClickListener(v -> {
            showIngredientePopup(context); // <--- esta línea reemplaza el Toast
        });


        return frameLayout;
    }





    private void mostrarIngredientesSeleccionados(List<Ingrediente> ingredientes) {
        containerIngredientes.removeAllViews();

        // Agregar solo los ingredientes que no estén en ingredientesSeleccionados
        for (Ingrediente ingrediente : ingredientes) {
            boolean existe = false;
            for (IngredienteReceta ir : ingredientesSeleccionados) {
                if (ir.getIngrediente().getId().equals(ingrediente.getId())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                IngredienteReceta ir = new IngredienteReceta();
                ir.setIngrediente(ingrediente);
                ir.setCantidad(0.0);  // cantidad inicial en 0
                ir.setUnidad("");      // unidad inicial vacía
                ingredientesSeleccionados.add(ir);
            }
        }

        // Mostrar cada IngredienteReceta con su cantidad y unidad actuales
        for (IngredienteReceta ir : ingredientesSeleccionados) {
            Ingrediente ingrediente = ir.getIngrediente();

            LinearLayout linearLayout = new LinearLayout(requireContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearParams.setMargins(0, 0, (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()), 0);
            linearLayout.setLayoutParams(linearParams);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            FrameLayout frameLayout = new FrameLayout(requireContext());
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
            int paddingInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            frameLayout.setLayoutParams(frameParams);
            frameLayout.setBackgroundResource(R.drawable.borde_ingrediente);
            frameLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

            ImageView imageView = new ImageView(requireContext());
            int imageSizeInDp = 60;
            int imageSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, imageSizeInDp, getResources().getDisplayMetrics());

            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    imageSizeInPx, imageSizeInPx);
            imageParams.gravity = Gravity.CENTER;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this)
                    .load(ingrediente.getImagenUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(imageView);

            // Al hacer click en la imagen se abre el popup para editar cantidad/unidad
            imageView.setOnClickListener(v -> {
                PopupIngrediente.newInstance(ir)
                        .show(getParentFragmentManager(), "popup_ingrediente");
            });

            frameLayout.addView(imageView);

            // Icono de tacho (esquina superior izquierda)
            ImageView iconoTacho = new ImageView(requireContext());
            FrameLayout.LayoutParams tachoParams = new FrameLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics())
            );
            tachoParams.gravity = Gravity.TOP | Gravity.START;
            tachoParams.setMargins(4, 0, 0, 0);
            iconoTacho.setLayoutParams(tachoParams);
            iconoTacho.setImageResource(R.drawable.iconotacho);

            // Click listener para eliminar ingrediente
            iconoTacho.setOnClickListener(v -> {
                ingredientesSeleccionados.remove(ir);
                mostrarIngredientesSeleccionados(new ArrayList<>()); // Refrescar la vista
            });

            frameLayout.addView(iconoTacho);

            // Icono de lápiz (esquina superior derecha)
            ImageView iconoArribaDerecha = new ImageView(requireContext());
            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics())
            );
            iconParams.gravity = Gravity.TOP | Gravity.END;
            iconParams.setMargins(0, 0, 4, 0);
            iconoArribaDerecha.setLayoutParams(iconParams);
            iconoArribaDerecha.setImageResource(R.drawable.lapiz1);

            // Click listener para editar ingrediente
            iconoArribaDerecha.setOnClickListener(v -> {
                showIngredientePopup(requireContext());
            });

            frameLayout.addView(iconoArribaDerecha);

            linearLayout.addView(frameLayout);

            TextView cantidadUnidadText = new TextView(requireContext());
            cantidadUnidadText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            cantidadUnidadText.setTextColor(Color.DKGRAY);
            cantidadUnidadText.setGravity(Gravity.CENTER);

            String cantidadStr = "";
            if (ir.getCantidad() != 0 && ir.getUnidad() != null && !ir.getUnidad().isEmpty()) {
                cantidadStr = ir.getCantidad() + " " + ir.getUnidad();
            } else if (ir.getCantidad() != 0) {
                cantidadStr = String.valueOf(ir.getCantidad());
            }
            cantidadUnidadText.setText(cantidadStr);

            linearLayout.addView(cantidadUnidadText);

            containerIngredientes.addView(linearLayout);
        }

        // Opcional: botón para agregar ingrediente (si tienes)
        FrameLayout addFrame = createAddIngredienteFrame(requireContext());
        containerIngredientes.addView(addFrame);
    }





    void enviarIngredientesReceta(Long recetaId) {
        if (ingredientesSeleccionados.isEmpty()) {
            Toast.makeText(requireContext(), "No hay ingredientes para agregar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar la lista para enviar (solo ID del ingrediente, cantidad y unidad)
        List<IngredienteReceta> datosParaEnviar = new ArrayList<>();

        for (IngredienteReceta original : ingredientesSeleccionados) {
            Ingrediente ingredienteSoloId = new Ingrediente();
            ingredienteSoloId.setId(original.getIngrediente().getId());

            IngredienteReceta copia = new IngredienteReceta();
            copia.setIngrediente(ingredienteSoloId);
            copia.setCantidad(original.getCantidad());
            copia.setUnidad(original.getUnidad());

            datosParaEnviar.add(copia);
        }

        // Log para ver JSON enviado
        Gson gson = new Gson();
        Log.d("JSON_Enviado", gson.toJson(datosParaEnviar));

        // Enviar la petición
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.agregarIngredientesReceta(recetaId, datosParaEnviar);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "¡Ingredientes agregados correctamente!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al agregar ingredientes", Toast.LENGTH_SHORT).show();
                    Log.e("API", "Error respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API", "Fallo: ", t);
            }
        });
    }

    private void enviarPasosReceta(Long recetaId) {
        if (pasosSeleccionados == null || pasosSeleccionados.isEmpty()) {
            Toast.makeText(requireContext(), "No hay pasos para agregar", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.agregarPasosReceta(recetaId, pasosSeleccionados);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("Pasos", "Pasos agregados correctamente");
                } else {
                    Toast.makeText(requireContext(), "Error al agregar pasos", Toast.LENGTH_SHORT).show();
                    Log.e("API", "Código de error pasos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo al conectar pasos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API", "Error pasos: ", t);
            }
        });
    }



    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


}
