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

public class TarjetaSedeFragment extends Fragment {

    private String nombreSede;
    private String telefonoSede;
    private String cantidadCursos;
    private String zona;           // Variable para la zona
    private String imagenUrl;

    public TarjetaSedeFragment() {}

    public static TarjetaSedeFragment newInstance(String nombreSede, String telefonoSede,
                                                  String cantidadCursos, String imagenUrl, String zona) {
        TarjetaSedeFragment fragment = new TarjetaSedeFragment();
        Bundle args = new Bundle();
        args.putString("nombreSede", nombreSede);
        args.putString("telefonoSede", telefonoSede);
        args.putString("cantidadCursos", cantidadCursos);
        args.putString("imagenUrl", imagenUrl);
        args.putString("zona", zona);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombreSede = getArguments().getString("nombreSede");
            telefonoSede = getArguments().getString("telefonoSede");
            cantidadCursos = getArguments().getString("cantidadCursos");
            imagenUrl = getArguments().getString("imagenUrl");
            zona = getArguments().getString("zona");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tarjeta_sede, container, false);

        TextView txtNombreSede = view.findViewById(R.id.txt_nombre_sede);
        TextView txtCantidadCursos = view.findViewById(R.id.txt_porciones);
        TextView txtTelefono = view.findViewById(R.id.telefono);
        ImageView imgSede = view.findViewById(R.id.imagen_curso);
        ImageView logoUbicacion = view.findViewById(R.id.logo_nuevo);
        TextView txtZona = view.findViewById(R.id.zona);  // Cambié nombre aquí

        txtNombreSede.setText(nombreSede != null ? nombreSede : "-");
        txtCantidadCursos.setText(cantidadCursos != null ? cantidadCursos : "- CURSOS");
        txtTelefono.setText(telefonoSede != null ? telefonoSede : "-");
        txtZona.setText(zona != null ? zona : "-");

        if (!TextUtils.isEmpty(imagenUrl)) {
            String urlImagenEscapada = imagenUrl.replace(" ", "%20");
            Glide.with(this)
                    .load(urlImagenEscapada)
                    .centerCrop()
                    .placeholder(R.drawable.imagen_default)
                    .error(R.drawable.imagen_default)
                    .into(imgSede);
        } else {
            imgSede.setImageResource(R.drawable.imagen_default);
        }

        return view;
    }
}