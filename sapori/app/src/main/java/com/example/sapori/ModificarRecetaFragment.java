package com.example.sapori;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteReceta;
import com.example.sapori.model.PasoReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModificarRecetaFragment extends Fragment {

    private int contadorPasos = 1;
    private EditText editTitulo;
    private LinearLayout btnAgregarFoto;
    private EditText editDescripcion;
    private LinearLayout btnSeleccionarTipo;
    private LinearLayout containerIngredientes;
    private EditText editPreparacion;

    private FrameLayout btnSumar;
    private FrameLayout btnRestar;
    private TextView tvCantidad;
    private LinearLayout containerPasos;
    private EditText editPasos;
    private LinearLayout layoutAgregarPaso;
    private ImageView imageViewSeleccionado;
    private TextView textViewSeleccionado;
    private List<PasoReceta> pasosSeleccionados = new ArrayList<>();

    private TextView btnModificcar;

    private TextView tipoPlatoSeleccionado;

    private ImageView flecha;
    private final List<Uri> imagenesSeleccionadas = new ArrayList<>();

    private int cantidadPorciones = 2;

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

    private Receta receta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modificar_receta, container, false);

        // Referencias

        containerPasos = view.findViewById(R.id.container_pasos);
        btnSeleccionarTipo = view.findViewById(R.id.btn_tipo_plato);
        btnModificcar = view.findViewById(R.id.btn_modificar_receta);
        editTitulo = view.findViewById(R.id.edit_titulo);
        btnAgregarFoto = view.findViewById(R.id.btn_agregar_foto);
        editDescripcion = view.findViewById(R.id.edit_descripcion);
        containerIngredientes = view.findViewById(R.id.container_ingredientes);
        editPreparacion = view.findViewById(R.id.edit_preparacion);
        btnSumar = view.findViewById(R.id.btnSumar);
        btnRestar = view.findViewById(R.id.btnRestar);
        tvCantidad = view.findViewById(R.id.tvCantidad);
        layoutAgregarPaso = view.findViewById(R.id.layout_agregar_paso);
        flecha = view.findViewById(R.id.flecha);

        // Obtener receta de argumentos
        if (getArguments() != null && getArguments().getSerializable("receta") != null) {
            receta = (Receta) getArguments().getSerializable("receta");
        }

        String tipoReceta = receta != null ? receta.getTipo() : null;

        // Cargar ingredientes
        if (receta.getIngredientes() != null) {
            List<Ingrediente> ingredientes = new ArrayList<>();
            for (IngredienteReceta ir : receta.getIngredientes()) {
                ingredientes.add(ir.getIngrediente());
            }
            mostrarIngredientesSeleccionados(ingredientes);
        }

        // Crear tipos de plato y marcar el seleccionado
        crearTiposDePlato(requireContext(), tipoReceta);

        if (receta != null) {
            editTitulo.setText(receta.getNombre());
            editDescripcion.setText(receta.getDescripcion());
            editPreparacion.setText(String.valueOf(receta.getTiempo()));
            cantidadPorciones = receta.getPorciones();
            tvCantidad.setText(String.valueOf(cantidadPorciones));

            // Cargar pasos
            if (receta.getPasos() != null) {
                for (PasoReceta paso : receta.getPasos()) {
                    EditText pasoView = crearEditTextPaso(paso.getDescripcion());
                    containerPasos.addView(pasoView);
                }

                // 👇 ACTUALIZAMOS EL CONTADOR CON LA CANTIDAD REAL DE PASOS + 1
                contadorPasos = receta.getPasos().size() + 1;
            }

            // Cargar imágenes
            if (receta.getFotosPlato() != null) {
                for (String url : receta.getFotosPlato()) {
                    FrameLayout frameLayout = createImageFrame(requireContext());
                    ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                    TextView textView = (TextView) frameLayout.getChildAt(1);

                    textView.setVisibility(View.GONE);
                    Glide.with(this).load(url).into(imageView);
                    imageView.setTag(url);
                    btnAgregarFoto.addView(frameLayout);
                }
            }

            FrameLayout addFotoFrame = createAddFotoFrame(requireContext());
            btnAgregarFoto.addView(addFotoFrame);

        } else {
            Log.e("ModificarRecetaFragment", "No se recibió objeto receta");
        }

        // Porciones
        tvCantidad.setText(String.valueOf(cantidadPorciones));
        btnSumar.setOnClickListener(v -> {
            cantidadPorciones++;
            tvCantidad.setText(String.valueOf(cantidadPorciones));
        });

        btnRestar.setOnClickListener(v -> {
            if (cantidadPorciones > 1) {
                cantidadPorciones--;
                tvCantidad.setText(String.valueOf(cantidadPorciones));
            }
        });

        // 👇 AGREGAR NUEVO PASO
        layoutAgregarPaso.setOnClickListener(v -> {
            EditText nuevoPaso = new EditText(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(150)
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

            // 👇 Incrementar después de agregar
            contadorPasos++;
        });

        flecha.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        btnModificcar.setOnClickListener(v -> {
            actualizarReceta();
        });

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
        Log.d("ModificarRecetaFragment", "Ingredientes seleccionados limpiados");
    }

    private void actualizarReceta() {
        if (tipoPlatoSeleccionado == null) {
            Toast.makeText(getContext(), "Error interno: tipo de plato no seleccionado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (receta == null || receta.getId() == null) {
            Toast.makeText(getContext(), "Error interno: receta inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoTitulo = editTitulo.getText().toString().trim();
        String nuevaDescripcion = editDescripcion.getText().toString().trim();
        String nuevoTipo = tipoPlatoSeleccionado.getText().toString().trim();
        int nuevoTiempo;

        try {
            nuevoTiempo = Integer.parseInt(editPreparacion.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Tiempo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear lista de pasos
        List<PasoReceta> nuevosPasos = new ArrayList<>();
        for (int i = 0; i < containerPasos.getChildCount(); i++) {
            View pasoView = containerPasos.getChildAt(i);
            if (pasoView instanceof EditText) {
                String textoPaso = ((EditText) pasoView).getText().toString().trim();
                if (!textoPaso.isEmpty()) {
                    PasoReceta paso = new PasoReceta();
                    paso.setDescripcion(textoPaso);
                    paso.setNumeroPaso(i + 1);
                    nuevosPasos.add(paso);
                }
            }
        }

        receta.setNombre(nuevoTitulo);
        receta.setDescripcion(nuevaDescripcion);
        receta.setTipo(nuevoTipo);
        receta.setTiempo(nuevoTiempo);
        receta.setPasos(nuevosPasos);
        receta.setPorciones(cantidadPorciones);
        receta.setFotosPlato(receta.getFotosPlato());

        // Logs imágenes
        if (receta.getFotosPlato() != null) {
            Log.d("ActualizarReceta", "Fotos antes de actualizar:");
            for (int i = 0; i < receta.getFotosPlato().size(); i++) {
                Log.d("ActualizarReceta", "Foto[" + i + "]: " + receta.getFotosPlato().get(i));
            }
        } else {
            Log.d("ActualizarReceta", "No hay fotos para actualizar.");
        }

        ApiService service = ApiClient.getApiService();
        Call<Receta> call = service.actualizarReceta(receta.getId(), receta);

        call.enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful()) {
                    // Actualización OK -> ahora enviamos pasos
                    pasosSeleccionados = nuevosPasos;

                    Log.d("DEBUG", "PasosSeleccionados size = " + pasosSeleccionados.size());
                    for (PasoReceta paso : pasosSeleccionados) {
                        Log.d("DEBUG", "Paso " + paso.getNumeroPaso() + ": " + paso.getDescripcion());
                    }

                    enviarPasosReceta(receta.getId());

                    enviarIngredientesReceta(receta.getId());

                    Toast.makeText(getContext(), "Receta actualizada con éxito", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private EditText crearEditTextPaso(String texto) {
        EditText paso = new EditText(requireContext());

        // LayoutParams con margen superior entre pasos
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(150)
        );
        params.topMargin = dpToPx(10); // espacio entre campos
        paso.setLayoutParams(params);

        paso.setText(texto);
        paso.setHint("Paso...");
        paso.setBackgroundResource(R.drawable.edittext_bg_rounded_7);
        paso.setPadding(dpToPx(15), dpToPx(9), dpToPx(16), dpToPx(9));
        paso.setTextSize(17);
        paso.setTextColor(Color.parseColor("#808080"));
        paso.setGravity(Gravity.TOP | Gravity.START);
        paso.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); // ✅
        paso.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));

        return paso;
    }

    private EditText crearEditTextIngrediente(String texto) {
        EditText ingrediente = new EditText(getContext());
        ingrediente.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        ingrediente.setText(texto);
        ingrediente.setHint("Ingrediente...");
        ingrediente.setBackgroundResource(R.drawable.edittext_bg_rounded_7);
        ingrediente.setPadding(15, 9, 16, 9);
        ingrediente.setTextSize(17);
        ingrediente.setTextColor(Color.parseColor("#808080"));
        ingrediente.setInputType(InputType.TYPE_CLASS_TEXT);
        ingrediente.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsregular));
        return ingrediente;
    }

    private FrameLayout createImageFrame(Context context) {
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
        params.setMargins(marginInPx, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);
        frameLayout.setBackgroundResource(R.drawable.borde_imagen);

        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        imageParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackground(null);
        imageView.setClipToOutline(true);

        TextView textView = new TextView(context);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParams);
        textView.setText("¡Sube aquí!");
        textView.setTextColor(Color.parseColor("#BCBCBC"));
        textView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsbold));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        View.OnClickListener abrirSelectorListener = v -> {
            imageViewSeleccionado = imageView;
            textViewSeleccionado = textView;
            abrirSelectorImagen();
        };

        imageView.setOnClickListener(abrirSelectorListener);
        textView.setOnClickListener(abrirSelectorListener);

        frameLayout.addView(imageView);
        frameLayout.addView(textView);

        return frameLayout;
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri imagenSeleccionadaUri = data.getData();
            if (imagenSeleccionadaUri == null) return;

            if (requestCode == 100) {
                if (imageViewSeleccionado != null) {
                    Glide.with(this).load(imagenSeleccionadaUri).into(imageViewSeleccionado);
                    if (textViewSeleccionado != null) {
                        textViewSeleccionado.setVisibility(View.GONE);
                    }

                    Object tag = imageViewSeleccionado.getTag();
                    if (tag instanceof Integer) {
                        int posicion = (Integer) tag;
                        if (receta != null) {
                            // Guardamos el URI local temporalmente
                            if (receta.getFotosPlato() == null) {
                                receta.setFotosPlato(new ArrayList<>());
                            }
                            if (posicion < receta.getFotosPlato().size()) {
                                receta.getFotosPlato().set(posicion, imagenSeleccionadaUri.toString());
                            } else {
                                receta.getFotosPlato().add(imagenSeleccionadaUri.toString());
                            }

                            // Ahora subimos la imagen al servidor
                            subirImagenRecetaAlServidor(imagenSeleccionadaUri, posicion);
                        }
                    }
                }

            } else if (requestCode == 101) {
                // Agregar nueva imagen
                if (receta != null) {
                    if (receta.getFotosPlato() == null) {
                        receta.setFotosPlato(new ArrayList<>());
                    }
                    receta.getFotosPlato().add(imagenSeleccionadaUri.toString());

                    FrameLayout frameLayout = createImageFrame(requireContext());
                    ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                    TextView textView = (TextView) frameLayout.getChildAt(1);

                    textView.setVisibility(View.GONE);
                    Glide.with(this).load(imagenSeleccionadaUri).into(imageView);

                    int newIndex = receta.getFotosPlato().size() - 1;
                    imageView.setTag(newIndex);

                    int botonIndex = btnAgregarFoto.getChildCount() - 1;
                    btnAgregarFoto.addView(frameLayout, botonIndex);

                    // Subir la nueva imagen al servidor
                    subirImagenRecetaAlServidor(imagenSeleccionadaUri, newIndex);
                }
            }
        }
    }

    private void subirImagenRecetaAlServidor(Uri imagenUri, int posicion) {
        if (receta == null || receta.getId() == null) return;

        // Convertir Uri a File
        File archivoImagen = uriToFile(imagenUri);
        if (archivoImagen == null) {
            Toast.makeText(getContext(), "Error al obtener archivo de imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear RequestBody y MultipartBody.Part
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivoImagen);
        MultipartBody.Part cuerpoImagen = MultipartBody.Part.createFormData("imagen", archivoImagen.getName(), requestFile);

        ApiService service = ApiClient.getApiService();
        Call<ResponseBody> call = service.subirImagenReceta(receta.getId(), cuerpoImagen);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // Asumamos que el servidor devuelve la URL de la imagen en el body (como texto plano)
                        String urlImagenSubida = response.body().string();

                        // Actualizar la lista con la URL real
                        receta.getFotosPlato().set(posicion, urlImagenSubida);
                        Log.d("ActualizarReceta", "Imagen subida correctamente: " + urlImagenSubida);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión al subir imagen: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para convertir Uri a File (puede variar según versión Android)
    private File uriToFile(Uri uri) {
        // Puedes usar FileUtil o convertir a InputStream y guardar en cache temporal
        // Aquí un ejemplo simple que funciona con content:// y file://
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
            tempFile.deleteOnExit();
            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FrameLayout createAddFotoFrame(Context context) {
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
        params.setMargins(marginInPx, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);

        frameLayout.setBackgroundResource(R.drawable.borde_imagen);

        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.icon_add);  // Aquí ponés el ícono para agregar (asegurate que exista)

        frameLayout.addView(imageView);

        frameLayout.setOnClickListener(v -> {
            // Abre el selector para agregar una nueva imagen
            abrirSelectorImagenNuevo();
        });

        return frameLayout;
    }

    // Método para abrir selector de imagen para nueva foto
    private void abrirSelectorImagenNuevo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 101);  // Usamos código 101 para agregar nueva foto
    }

    private void crearTiposDePlato(Context context, String tipoSeleccionado) {
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

            // Marcar tipo seleccionado con fondo verde y texto blanco
            if (tipo.equals(tipoSeleccionado)) {
                layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_seleccionado);
                icono.setColorFilter(Color.WHITE);
                tipoView.setTextColor(Color.WHITE);

                layoutTipoSeleccionado[0] = layoutTipo;
                iconoSeleccionado[0] = icono;
                tipoPlatoSeleccionado = tipoView;
            } else {
                layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_no_seleccionado);
            }

            layoutTipo.setOnClickListener(v -> {
                if (layoutTipoSeleccionado[0] == layoutTipo) {
                    layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_no_seleccionado);

                    Object tag = icono.getTag();
                    if (tag instanceof Integer) {
                        icono.setImageResource((Integer) tag);
                    }
                    icono.setColorFilter(null);

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
                        iconoSeleccionado[0].setColorFilter(null);
                    }

                    TextView textoAnterior = (TextView) layoutTipoSeleccionado[0].getChildAt(1);
                    textoAnterior.setTextColor(Color.DKGRAY);
                }

                layoutTipo.setBackgroundResource(R.drawable.bg_tipo_plato_seleccionado);
                icono.setColorFilter(Color.WHITE);
                tipoView.setTextColor(Color.WHITE);

                layoutTipoSeleccionado[0] = layoutTipo;
                iconoSeleccionado[0] = icono;
                tipoPlatoSeleccionado = tipoView;
            });

            btnSeleccionarTipo.addView(layoutTipo);
        }
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

    private FrameLayout createIngredienteFrame(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);

        int sizeInDp = 120, marginInDp = 8, paddingInDp = 6;
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingInDp, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);

        frameLayout.setBackgroundResource(R.drawable.borde_ingrediente);
        frameLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

        ImageView icono = new ImageView(context);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = Gravity.CENTER;
        icono.setLayoutParams(iconParams);
        icono.setImageResource(R.drawable.icon_add);

        icono.setOnClickListener(v -> showIngredientePopup(context));

        frameLayout.addView(icono);
        return frameLayout;
    }

    private FrameLayout createAddIngredienteFrame(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);

        int sizeInDp = 120, marginInDp = 8, paddingInDp = 6;
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());
        int paddingInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingInDp, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, marginInPx, 0);
        frameLayout.setLayoutParams(params);
        frameLayout.setBackgroundResource(R.drawable.borde_imagen);

        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        imageParams.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.icon_add);

        frameLayout.addView(imageView);
        frameLayout.setOnClickListener(v -> showIngredientePopup(context));

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
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Ingrediente>> call, Throwable t) {
                                    Log.e("API", "Error al obtener ingredientes", t);
                                }
                            });
                        }
                    }
            );

            IngredientesFragment2 ingredientesFragment2 = new IngredientesFragment2();
            ingredientesFragment2.show(activity.getSupportFragmentManager(), "ingredientes_fragment");
        }
    }

    private void mostrarIngredientesSeleccionados(List<Ingrediente> ingredientes) {
        containerIngredientes.removeAllViews();

        for (Ingrediente ingrediente : ingredientes) {
            FrameLayout frameLayout = new FrameLayout(requireContext());

            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
            int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            int paddingInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            params.setMargins(0, 0, marginInPx, 0);
            frameLayout.setLayoutParams(params);
            frameLayout.setBackgroundResource(R.drawable.borde_ingrediente);
            frameLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

            // Crear el IngredienteReceta asociado
            IngredienteReceta ingredienteReceta = new IngredienteReceta();
            ingredienteReceta.setIngrediente(ingrediente);

            // Imagen principal
            // Imagen principal
            ImageView imageView = new ImageView(requireContext());
            int imageSizeInDp = 60;
            int imageSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, imageSizeInDp, getResources().getDisplayMetrics());

            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    imageSizeInPx, imageSizeInPx);
            imageParams.gravity = Gravity.CENTER;

            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

// 🔥 ¡Acá seteás el tag!
            imageView.setTag(ingredienteReceta); // <<--- ESTA ES LA LÍNEA CLAVE

            Glide.with(this)
                    .load(ingrediente.getImagenUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(imageView);

            frameLayout.addView(imageView);


            // Icono lápiz arriba a la derecha
            ImageView iconoArribaDerecha = new ImageView(requireContext());
            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics())
            );
            iconParams.gravity = Gravity.TOP | Gravity.END;
            iconParams.setMargins(0, 0, 4, 0);
            iconoArribaDerecha.setLayoutParams(iconParams);
            iconoArribaDerecha.setImageResource(R.drawable.lapiz1);

            // El lápiz también abre el popup
            iconoArribaDerecha.setOnClickListener(v -> {
                PopupIngrediente.newInstance(ingredienteReceta)
                        .show(getParentFragmentManager(), "popup_ingrediente");
            });

            frameLayout.addView(iconoArribaDerecha);

            // Al tocar el fondo (frame completo), también se abre el popup
            frameLayout.setOnClickListener(v -> {
                PopupIngrediente.newInstance(ingredienteReceta)
                        .show(getParentFragmentManager(), "popup_ingrediente");
            });

            containerIngredientes.addView(frameLayout);
        }

        FrameLayout addFrame = createAddIngredienteFrame(requireContext());
        containerIngredientes.addView(addFrame);
    }

    private void enviarIngredientesReceta(Long recetaId) {
        List<IngredienteReceta> datosParaEnviar = new ArrayList<>();

        // Excluir el último frame que es el "+" para agregar
        for (int i = 0; i < containerIngredientes.getChildCount() - 1; i++) {
            FrameLayout frameLayout = (FrameLayout) containerIngredientes.getChildAt(i);
            ImageView imageView = (ImageView) frameLayout.getChildAt(0);

            Object tag = imageView.getTag();
            if (tag instanceof IngredienteReceta) {
                IngredienteReceta original = (IngredienteReceta) tag;

                if (original.getIngrediente() == null) {
                    Log.e("🌶️ [ENVIAR_ING]", "❌ Ingrediente es null en índice " + i);
                    continue;
                }

                Long ingredienteId = original.getIngrediente().getId();
                if (ingredienteId == null) {
                    Log.e("🌶️ [ENVIAR_ING]", "❌ ID del ingrediente es null en índice " + i);
                    continue;
                }

                Ingrediente ingredienteSoloId = new Ingrediente();
                ingredienteSoloId.setId(ingredienteId);

                IngredienteReceta copia = new IngredienteReceta();
                copia.setIngrediente(ingredienteSoloId);
                copia.setCantidad(original.getCantidad());
                copia.setUnidad(original.getUnidad());

                Log.d("🌶️ [ENVIAR_ING]", "✅ Ingrediente preparado: ID=" + ingredienteId + ", Cantidad=" + original.getCantidad() + ", Unidad=" + original.getUnidad());

                datosParaEnviar.add(copia);
            } else {
                Log.w("🌶️ [ENVIAR_ING]", "⚠️ El tag no es un IngredienteReceta válido en índice " + i);
            }
        }

        if (datosParaEnviar.isEmpty()) {
            Log.e("🌶️ [ENVIAR_ING]", "❌ No se encontraron ingredientes válidos para enviar");
            return;
        }

        // Mostrar el JSON generado
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonDebug = gson.toJson(datosParaEnviar);
        Log.d("🌶️ [ENVIAR_ING]", "📦 JSON que se enviará:\n" + jsonDebug);

        // Llamada a la API
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.agregarIngredientesReceta(recetaId, datosParaEnviar);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("🌶️ [ENVIAR_ING]", "✅ Ingredientes enviados correctamente (HTTP " + response.code() + ")");
                } else {
                    Log.e("🌶️ [ENVIAR_ING]", "❌ Error al enviar ingredientes: código HTTP " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("🌶️ [ENVIAR_ING]", "🧾 ErrorBody: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e("🌶️ [ENVIAR_ING]", "⚠️ Error al leer el errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("🌶️ [ENVIAR_ING]", "❌ Fallo de red o excepción: " + t.getMessage(), t);
            }
        });
    }
}
