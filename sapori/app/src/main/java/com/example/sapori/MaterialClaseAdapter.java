package com.example.sapori;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.MaterialDeClase;
import com.example.sapori.network.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaterialClaseAdapter extends RecyclerView.Adapter<MaterialClaseAdapter.ViewHolder> {
    private final List<MaterialDeClase> lista;
    private final ApiService apiService;
    private final Context context;

    public MaterialClaseAdapter(List<MaterialDeClase> lista, ApiService apiService, Context context) {
        this.lista = lista;
        this.apiService = apiService;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material_clase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaterialDeClase item = lista.get(position);
        holder.txtNombre.setText(item.getNombre());
        holder.txtFecha.setText(formatearFecha(item.getFecha()));
        holder.txtTamanio.setText(formatearTamanio(item.getTamanio()));
        holder.btnDescargar.setOnClickListener(v -> descargarArchivo(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha, txtTamanio;
        ImageView btnDescargar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtTamanio = itemView.findViewById(R.id.txtTamanio);
            btnDescargar = itemView.findViewById(R.id.btnDescargar);
        }
    }

    private String formatearFecha(String fechaISO) {
        try {
            if (fechaISO == null) return "-";
            String soloFecha = fechaISO.length() > 10 ? fechaISO.substring(0, 10) : fechaISO;
            String[] partes = soloFecha.split("-");
            if (partes.length == 3) {
                return partes[2] + "-" + partes[1] + "-" + partes[0];
            }
            return soloFecha;
        } catch (Exception e) {
            return fechaISO;
        }
    }

    private String formatearTamanio(int tamanio) {
        if (tamanio >= 1024) {
            return (tamanio / 1024) + "MB";
        } else {
            return tamanio + "KB";
        }
    }

    private void descargarArchivo(MaterialDeClase material) {
        String url = material.getArchivoUrl();
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                intent.setPackage(null);
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(context, "No hay archivo para descargar", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarArchivo(ResponseBody body, String nombreArchivo) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File archivo = new File(downloadsDir, nombreArchivo);
            InputStream inputStream = body.byteStream();
            FileOutputStream outputStream = new FileOutputStream(archivo);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            Toast.makeText(context, "Archivo descargado en Descargas", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
    }
}
