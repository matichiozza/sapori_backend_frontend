package com.example.sapori;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class TarjetaCursoFragment extends Fragment {

    private String nombreCurso;
    private String modalidad;
    private String precio;
    private String duracion;
    private String imagenUrl;
    private String fecha; // uso interno

    public TarjetaCursoFragment() { }

    public static TarjetaCursoFragment newInstance(String nombreCurso, String modalidad, String precio,
                                                   String duracion, String imagenUrl, String fecha) {
        TarjetaCursoFragment fragment = new TarjetaCursoFragment();
        Bundle args = new Bundle();
        args.putString("nombreCurso", nombreCurso);
        args.putString("modalidad", modalidad);
        args.putString("precio", precio);
        args.putString("duracion", duracion);
        args.putString("imagenUrl", imagenUrl);
        args.putString("fecha", fecha);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombreCurso = getArguments().getString("nombreCurso");
            modalidad = getArguments().getString("modalidad");
            precio = getArguments().getString("precio");
            duracion = getArguments().getString("duracion");
            imagenUrl = getArguments().getString("imagenUrl");
            fecha = getArguments().getString("fecha"); // uso interno
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarjeta_curso, container, false);

        TextView txtNombreCurso = view.findViewById(R.id.txt_nombre_curso);
        TextView txtModalidad = view.findViewById(R.id.modalidad);
        TextView txtPorciones = view.findViewById(R.id.txt_porciones);
        TextView txtDuracion = view.findViewById(R.id.duracion);
        ImageView imgCurso = view.findViewById(R.id.imagen_curso);

        ImageView logoPresencial = view.findViewById(R.id.logo_presencial);
        ImageView logoNuevo = view.findViewById(R.id.logo_nuevo);
        TextView modalidad2 = view.findViewById(R.id.modalidad2);

        txtNombreCurso.setText(nombreCurso != null ? nombreCurso : "-");
        txtModalidad.setText(modalidad != null ? modalidad : "-");
        txtPorciones.setText(precio != null ? precio : "-");
        txtDuracion.setText(duracion != null ? duracion : "-");

        if (!TextUtils.isEmpty(imagenUrl)) {
            String urlImagenEscapada = imagenUrl.replace(" ", "%20");
            Glide.with(this)
                    .load(urlImagenEscapada)
                    .centerCrop()
                    .placeholder(R.drawable.imagen_default)
                    .error(R.drawable.imagen_default)
                    .into(imgCurso);
        } else {
            imgCurso.setImageResource(R.drawable.imagen_default);
        }

        // Log para ver valor crudo de modalidad
        Log.d("TarjetaCursoFragment", "Modalidad raw: '" + modalidad + "'");
        if (modalidad != null) {
            Log.d("TarjetaCursoFragment", "Modalidad chars: " + modalidad.chars().toArray());
        }

        Log.d("TarjetaCursoFragment", "Modalidad recibida: '" + modalidad + "'");
        String mod = modalidad != null ? modalidad.trim().toUpperCase() : "";

        if (mod.equals("PRESENCIAL")) {
            logoPresencial.setVisibility(View.VISIBLE);
            logoNuevo.setVisibility(View.GONE);
            modalidad2.setVisibility(View.GONE);
        } else if (mod.equals("VIRTUAL")) {
            logoPresencial.setVisibility(View.GONE);
            logoNuevo.setVisibility(View.VISIBLE);
            modalidad2.setVisibility(View.VISIBLE);
        } else {
            logoPresencial.setVisibility(View.GONE);
            logoNuevo.setVisibility(View.GONE);
            modalidad2.setVisibility(View.GONE);
        }

        return view;
    }

    public void actualizarModalidad(String nuevaModalidad) {
        this.modalidad = nuevaModalidad;
        View view = getView();
        if (view != null) {
            TextView txtModalidad = view.findViewById(R.id.modalidad);
            txtModalidad.setText(nuevaModalidad != null ? nuevaModalidad : "-");
        }
    }
}