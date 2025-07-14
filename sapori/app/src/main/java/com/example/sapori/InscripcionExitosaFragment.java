package com.example.sapori;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InscripcionExitosaFragment extends Fragment {
    private static final String ARG_CURSO = "curso";
    private static final String ARG_SEDE = "sede";
    private static final String ARG_DURACION = "duracion";

    private String curso;
    private String sede;
    private String duracion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inscripcion_exitosa, container, false);

        if (getArguments() != null) {
            curso = getArguments().getString(ARG_CURSO, "");
            sede = getArguments().getString(ARG_SEDE, "");
            duracion = getArguments().getString(ARG_DURACION, "");
        }

        TextView txtCurso = view.findViewById(R.id.txtCurso);
        TextView txtSede = view.findViewById(R.id.txtSede);
        TextView txtDuracion = view.findViewById(R.id.txtDuracion);
        TextView btnIrAlCurso = view.findViewById(R.id.btnIrAlCurso);
        LinearLayout tarjetaContainer = view.findViewById(R.id.tarjeta_container);

        txtCurso.setText(curso);
        txtSede.setText(sede);
        txtDuracion.setText(duracion);
        btnIrAlCurso.setOnClickListener(v -> { /* Acción para ir al curso */ });

        ApiService apiService = ApiClient.getApiService();
        apiService.obtenerCursosParaTarjetas().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    tarjetaContainer.removeAllViews();

                    for (Map<String, Object> curso : response.body()) {
                        View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_curso, tarjetaContainer, false);

                        TextView txtNombreCurso = tarjeta.findViewById(R.id.txt_nombre_curso);
                        TextView txtModalidad = tarjeta.findViewById(R.id.modalidad);
                        TextView txtPrecio = tarjeta.findViewById(R.id.txt_porciones);  // Aquí usas este TextView para precio
                        TextView txtDuracion = tarjeta.findViewById(R.id.duracion);
                        ImageView imagenCurso = tarjeta.findViewById(R.id.imagen_curso);

                        // Claves según JSON
                        String nombreCurso = (String) curso.get("nombre");
                        String modalidad = (String) curso.get("modalidad");
                        String precio = String.valueOf(curso.get("importe"));

                        // Duración sin decimales y en semanas
                        Object duracionObj = curso.get("duracion");
                        String duracionStr = "-";
                        if (duracionObj instanceof Number) {
                            int duracionInt = ((Number) duracionObj).intValue();
                            duracionStr = duracionInt + " semanas";
                        }

                        txtNombreCurso.setText(nombreCurso != null ? nombreCurso : "-");
                        txtModalidad.setText(modalidad != null ? modalidad : "-");
                        txtPrecio.setText(precio != null ? precio : "-");
                        txtDuracion.setText(duracionStr);

                        // fotoPrincipal es una lista, tomamos la primera imagen si existe
                        Object fotoPrincipalObj = curso.get("fotoPrincipal");
                        String urlImagen = null;
                        if (fotoPrincipalObj instanceof List) {
                            List<?> fotos = (List<?>) fotoPrincipalObj;
                            if (!fotos.isEmpty() && fotos.get(0) instanceof String) {
                                urlImagen = (String) fotos.get(0);
                            }
                        }

                        if (urlImagen != null && !urlImagen.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(urlImagen.replace(" ", "%20"))
                                    .placeholder(R.drawable.imagen_default)
                                    .error(R.drawable.imagen_default)
                                    .into(imagenCurso);
                        } else {
                            imagenCurso.setImageResource(R.drawable.imagen_default);
                        }

                        tarjetaContainer.addView(tarjeta);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Manejo de error
                t.printStackTrace();
            }
        });

        return view;
    }

    public static InscripcionExitosaFragment newInstance(String curso, String sede, String duracion) {
        InscripcionExitosaFragment fragment = new InscripcionExitosaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURSO, curso);
        args.putString(ARG_SEDE, sede);
        args.putString(ARG_DURACION, duracion);
        fragment.setArguments(args);
        return fragment;
    }
}
