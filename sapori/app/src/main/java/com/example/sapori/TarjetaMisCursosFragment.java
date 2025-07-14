package com.example.sapori;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class TarjetaMisCursosFragment extends Fragment {

    private String nombreCurso;
    private String modalidad;
    private String precio;
    private String cursadaHorarioDia;  // antes era duracion
    private String imagenUrl;
    private String estadoCurso;

    public TarjetaMisCursosFragment() {}

    public static TarjetaMisCursosFragment newInstance(String nombreCurso, String modalidad, String precio,
                                                       String cursadaHorarioDia, String imagenUrl, String estadoCurso) {
        TarjetaMisCursosFragment fragment = new TarjetaMisCursosFragment();
        Bundle args = new Bundle();
        args.putString("nombreCurso", nombreCurso);
        args.putString("modalidad", modalidad);
        args.putString("precio", precio);
        args.putString("cursadaHorarioDia", cursadaHorarioDia);
        args.putString("imagenUrl", imagenUrl);
        args.putString("estadoCurso", estadoCurso);
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
            cursadaHorarioDia = getArguments().getString("cursadaHorarioDia");
            imagenUrl = getArguments().getString("imagenUrl");
            estadoCurso = getArguments().getString("estadoCurso");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarjeta_mis_cursos, container, false);

        TextView txtNombreCurso = view.findViewById(R.id.txt_nombre_curso);
        TextView txtModalidad = view.findViewById(R.id.modalidad);
        TextView txtDuracion = view.findViewById(R.id.duracion);  // acá sigue llamándose duracion en el layout, está bien
        ImageView imgCurso = view.findViewById(R.id.imagen_curso);

        // Imagenes para estado del curso
        ImageView puntoVerde = view.findViewById(R.id.punto_verde);
        ImageView puntoRojo = view.findViewById(R.id.punto_rojo);
        ImageView puntoAmarillo = view.findViewById(R.id.punto_amarillo);

        // TextViews para estado del curso
        TextView txtEnCurso = view.findViewById(R.id.txt_en_curso);
        TextView txtFinalizado = view.findViewById(R.id.txt_en_curso2);
        TextView txtProximamente = view.findViewById(R.id.txt_en_curso1);

        // Logos modalidad
        ImageView logoPresencial = view.findViewById(R.id.logo_presencial);
        ImageView logoNuevo = view.findViewById(R.id.logo_nuevo);
        TextView modalidad2 = view.findViewById(R.id.modalidad2);

        // Seteo datos generales
        txtNombreCurso.setText(nombreCurso != null ? nombreCurso : "-");
        txtModalidad.setText(modalidad != null ? modalidad : "-");
        txtDuracion.setText(cursadaHorarioDia != null ? cursadaHorarioDia : "-");

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

        // Mostrar estado del curso
        if (estadoCurso != null) {
            String estado = estadoCurso.trim().toLowerCase();
            switch (estado) {
                case "en curso":
                    puntoVerde.setVisibility(View.VISIBLE);
                    txtEnCurso.setVisibility(View.VISIBLE);
                    break;
                case "finalizado":
                    puntoRojo.setVisibility(View.VISIBLE);
                    txtFinalizado.setVisibility(View.VISIBLE);
                    break;
                case "proximamente":
                    puntoAmarillo.setVisibility(View.VISIBLE);
                    txtProximamente.setVisibility(View.VISIBLE);
                    break;
            }
        }

        // Mostrar logos de modalidad
        String mod = modalidad != null ? modalidad.trim().toUpperCase() : "";

        if (mod.equals("PRESENCIAL")) {
            logoPresencial.setVisibility(View.VISIBLE);
            logoNuevo.setVisibility(View.GONE);
            modalidad2.setVisibility(View.GONE);
            txtModalidad.setVisibility(View.VISIBLE);
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
}