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

public class TarjetaRecetaFragment extends Fragment {

    private String nombre;
    private String porciones;
    private String tipo;
    private String fecha;       // La fecha queda guardada pero no usada en UI
    private String imagenUrl;
    private String autor;
    private String tiempo;
    private String calificacion;

    public TarjetaRecetaFragment() { }

    public static TarjetaRecetaFragment newInstance(String nombre, String porciones, String tipo, String fecha,
                                                    String imagenUrl, String autor, String tiempo, String calificacion) {
        TarjetaRecetaFragment fragment = new TarjetaRecetaFragment();
        Bundle args = new Bundle();
        args.putString("nombre", nombre);
        args.putString("porciones", porciones);
        args.putString("tipo", tipo);
        args.putString("fecha", fecha);      // Guardás la fecha para uso interno, no UI
        args.putString("imagenUrl", imagenUrl);
        args.putString("autor", autor);
        args.putString("tiempo", tiempo);
        args.putString("calificacion", calificacion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombre = getArguments().getString("nombre");
            porciones = getArguments().getString("porciones");
            tipo = getArguments().getString("tipo");
            fecha = getArguments().getString("fecha");  // La guardás para uso interno
            imagenUrl = getArguments().getString("imagenUrl");
            autor = getArguments().getString("autor");
            tiempo = getArguments().getString("tiempo");
            calificacion = getArguments().getString("calificacion");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarjeta_receta, container, false);

        TextView txtNombre = view.findViewById(R.id.txt_nombre);
        TextView txtPorciones = view.findViewById(R.id.txt_porciones);
        TextView txtAutor = view.findViewById(R.id.nombreautor);
        TextView txtTiempo = view.findViewById(R.id.tiempo);
        TextView txtCalificacion = view.findViewById(R.id.calificacion);

        ImageView imagen = view.findViewById(R.id.imagen_receta);

        txtNombre.setText(nombre != null ? nombre : "-");
        txtPorciones.setText(porciones != null ? porciones : "-");
        txtAutor.setText(autor != null ? autor : "Desconocido");
        txtTiempo.setText(tiempo != null ? tiempo : "-");
        txtCalificacion.setText(calificacion != null ? calificacion : "-");

        if (!TextUtils.isEmpty(imagenUrl)) {
            String urlImagenEscapada = imagenUrl.replace(" ", "%20");
            Glide.with(this)
                    .load(urlImagenEscapada)
                    .centerCrop()
                    .placeholder(R.drawable.imagen_default)
                    .error(R.drawable.imagen_default)
                    .into(imagen);
        } else {
            imagen.setImageResource(R.drawable.imagen_default);
        }

        // Fecha guardada pero no se muestra en UI ni XML

        return view;
    }

    /**
     * Método público para actualizar la calificación visible en la tarjeta.
     * Solo funcionará si el fragmento está creado y visible.
     */
    public void actualizarCalificacion(String nuevaCalificacion) {
        this.calificacion = nuevaCalificacion;
        View view = getView();
        if (view != null) {
            TextView txtCalificacion = view.findViewById(R.id.calificacion);
            txtCalificacion.setText(nuevaCalificacion != null ? nuevaCalificacion : "-");
        }
    }

    // Método para usar la fecha internamente si necesitás
    private void procesarFecha(String fecha) {
        // Lógica interna con la fecha, por ejemplo, validaciones o cálculos
    }
}
