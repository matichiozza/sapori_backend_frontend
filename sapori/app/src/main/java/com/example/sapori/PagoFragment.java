package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.model.Curso;
import com.example.sapori.model.CursoSede;
import com.example.sapori.model.Sede;
import com.example.sapori.model.Alumno;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagoFragment extends Fragment {
    private static final String ARG_CURSO = "curso";
    private static final String ARG_SEDE = "sede";

    private Curso curso;
    private CursoSede cursoSede;
    private TextView txtNumeroTarjeta, txtPrecioOriginal, txtDescuento, txtPrecioFinal;
    private EditText editCodigoSeguridad;
    private TextView btnVerResumen;

    private String numeroTarjeta;
    private int codigoSeguridad;

    public PagoFragment() {
        super(R.layout.fragment_pago);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curso = (Curso) getArguments().getSerializable(ARG_CURSO);
            cursoSede = (CursoSede) getArguments().getSerializable(ARG_SEDE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pago, container, false);

        txtNumeroTarjeta = view.findViewById(R.id.txtNumeroTarjeta);
        txtPrecioOriginal = view.findViewById(R.id.txtPrecioOriginal);
        txtDescuento = view.findViewById(R.id.txtDescuento);
        txtPrecioFinal = view.findViewById(R.id.txtPrecioFinal);
        editCodigoSeguridad = view.findViewById(R.id.editCodigoSeguridad);
        btnVerResumen = view.findViewById(R.id.btnVerResumen);
        ImageView btnAtras = view.findViewById(R.id.btnAtras);

        // Obtener datos del alumno desde SharedPreferences (ajustar si se obtiene de otro lado)
        SharedPreferences prefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);

        ApiService apiService = ApiClient.getApiService();
        apiService.obtenerAlumnoPorId(usuarioId).enqueue(new Callback<Alumno>() {
            @Override
            public void onResponse(Call<Alumno> call, Response<Alumno> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Alumno alumno = response.body();
                    numeroTarjeta = alumno.getNumTarjetaCredito();
                    codigoSeguridad = alumno.getCodigoSeguridad();
                    Log.d("PagoFragment", "Número de tarjeta recibido: " + numeroTarjeta);
                    Log.d("PagoFragment", "Código de seguridad recibido: " + codigoSeguridad);

                    if (numeroTarjeta != null && numeroTarjeta.length() >= 4) {
                        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);
                        txtNumeroTarjeta.setText("**** **** **** " + ultimos4);
                    } else {
                        txtNumeroTarjeta.setText("**** **** **** 0000");
                    }
                }
            }
            @Override
            public void onFailure(Call<Alumno> call, Throwable t) {
                Log.e("PagoFragment", "Error obteniendo alumno", t);
            }
        });

        // Mostrar precios y descuento
        final float precio = curso != null && curso.getImporte() != null ? curso.getImporte() : 0f;
        final boolean hayDescuento = cursoSede != null && cursoSede.isHayDescuento() && cursoSede.getDescuento() > 0;
        final int descuento = hayDescuento ? cursoSede.getDescuento() : 0;
        final float precioFinal = hayDescuento ? precio * (1 - descuento / 100f) : precio;

        if (hayDescuento) {
            txtPrecioOriginal.setVisibility(View.VISIBLE);
            txtPrecioOriginal.setText("$" + String.format("%.0f", precio));
            txtPrecioOriginal.setPaintFlags(txtPrecioOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            txtDescuento.setVisibility(View.VISIBLE);
            txtDescuento.setText(descuento + "% OFF");
        } else {
            txtPrecioOriginal.setVisibility(View.GONE);
            txtDescuento.setVisibility(View.GONE);
        }
        txtPrecioFinal.setText("$" + String.format("%.3f", precioFinal));

        btnVerResumen.setOnClickListener(v -> {
            String codigoIngresado = editCodigoSeguridad.getText().toString();
            if (TextUtils.isEmpty(codigoIngresado)) {
                Toast.makeText(requireContext(), "Ingrese el código de seguridad", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!codigoIngresado.equals(String.valueOf(codigoSeguridad))) {
                Toast.makeText(requireContext(), "Código de seguridad incorrecto", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navegar al fragmento de resumen
            Bundle args = new Bundle();
            args.putSerializable("curso", curso);
            args.putSerializable("sede", cursoSede);
            args.putString("tarjeta", numeroTarjeta != null ? numeroTarjeta : "");
            args.putFloat("precio", precioFinal);
            args.putFloat("precioOriginal", precio);
            args.putInt("descuento", descuento);
            Navigation.findNavController(view).navigate(R.id.action_pagoFragment_to_resumenInscripcionFragment, args);
        });

        btnAtras.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        return view;
    }

    // Métodos para crear el fragmento con argumentos
    public static PagoFragment newInstance(Curso curso, CursoSede cursoSede) {
        PagoFragment fragment = new PagoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURSO, curso);
        args.putSerializable(ARG_SEDE, cursoSede);
        fragment.setArguments(args);
        return fragment;
    }
} 