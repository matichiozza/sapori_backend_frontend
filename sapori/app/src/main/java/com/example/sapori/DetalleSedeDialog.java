package com.example.sapori;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleSedeDialog extends Dialog {

    private Context context;
    private Long sedeId;
    private String urlImagenSede;

    private TextView nombreSedeDialog, direccionDialog, telefonoDialog;
    private ImageView imagenSedeDialog, btnCerrarDialog;

    public DetalleSedeDialog(@NonNull Context context, Long sedeId, String urlImagenSede) {
        super(context);
        this.context = context;
        this.sedeId = sedeId;
        this.urlImagenSede = urlImagenSede;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_detalle_sede);

        // Inicializar vistas
        nombreSedeDialog = findViewById(R.id.nombreSedeDialog);
        direccionDialog = findViewById(R.id.direccionDialog);
        telefonoDialog = findViewById(R.id.telefonoDialog);
        imagenSedeDialog = findViewById(R.id.imagenSedeDialog);
        btnCerrarDialog = findViewById(R.id.btnCerrarDialog);

        // Configurar botón cerrar
        btnCerrarDialog.setOnClickListener(v -> dismiss());

        // Cargar datos
        cargarDatosSede();
        cargarImagenSede();
    }

    private void cargarDatosSede() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.obtenerSedesParaTarjetas();

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> sede : response.body()) {
                        Number id = (Number) sede.get("id");
                        if (id != null && id.longValue() == sedeId) {
                            String nombre = (String) sede.get("nombre");
                            String direccion = (String) sede.get("direccion");
                            String telefono = (String) sede.get("telefono");

                            nombreSedeDialog.setText(nombre != null ? nombre : "Nombre no disponible");
                            direccionDialog.setText("Dirección: " + (direccion != null ? direccion : "No disponible"));
                            telefonoDialog.setText("Teléfono: " + (telefono != null ? telefono : "No disponible"));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("DetalleSedeDialog", "Error al obtener sede: " + t.getMessage());
            }
        });
    }

    private void cargarImagenSede() {
        if (urlImagenSede != null && !urlImagenSede.isEmpty()) {
            Glide.with(context)
                    .load(urlImagenSede.replace(" ", "%20"))
                    .placeholder(R.drawable.imagen_default)
                    .error(R.drawable.imagen_default)
                    .into(imagenSedeDialog);
        } else {
            imagenSedeDialog.setImageResource(R.drawable.imagen_default);
        }
    }
} 