package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Alumno;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalizarRegistroFragment extends Fragment {

    private EditText etNumeroTramite;
    private Uri uriFrente, uriDorso;
    private ImageView ivPlusFrente, ivPlusDorso;

    public FinalizarRegistroFragment() {
        super(R.layout.fragment_finalizar_registro);
    }

    private final ActivityResultLauncher<String> launcherFrente =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriFrente = uri;
                    Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.icon_add) // o el ícono que quieras
                            .into(ivPlusFrente);
                }
            });

    private final ActivityResultLauncher<String> launcherDorso =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriDorso = uri;
                    Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.icon_add)
                            .into(ivPlusDorso);
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNumeroTramite = view.findViewById(R.id.et_numero_tramite);
        ivPlusFrente = view.findViewById(R.id.iv_plus_frente);
        ivPlusDorso = view.findViewById(R.id.iv_plus_dorso);

        FrameLayout frameFrente = view.findViewById(R.id.frame_dni_frente);
        FrameLayout frameDorso = view.findViewById(R.id.frame_dni_dorso);
        LinearLayout btnFinalizar = view.findViewById(R.id.btn_finalizar_registro);

        frameFrente.setOnClickListener(v -> launcherFrente.launch("image/*"));
        frameDorso.setOnClickListener(v -> launcherDorso.launch("image/*"));
        btnFinalizar.setOnClickListener(this::finalizarRegistro);
    }

    private void finalizarRegistro(View view) {
        String tramite = etNumeroTramite.getText().toString().trim();

        if (tramite.isEmpty() || uriFrente == null || uriDorso == null) {
            Toast.makeText(requireContext(), "Completa todos los campos y selecciona ambas imágenes", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);

        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileFrente = PathUtil.getFileFromUri(requireContext(), uriFrente);
        File fileDorso = PathUtil.getFileFromUri(requireContext(), uriDorso);

        if (fileFrente == null || !fileFrente.exists() || fileDorso == null || !fileDorso.exists()) {
            Toast.makeText(requireContext(), "Error al leer las imágenes seleccionadas", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(usuarioId));
        RequestBody tramiteBody = RequestBody.create(MediaType.parse("text/plain"), tramite);

        RequestBody frenteBody = RequestBody.create(MediaType.parse("image/*"), fileFrente);
        MultipartBody.Part frentePart = MultipartBody.Part.createFormData("imagenDniFrente", fileFrente.getName(), frenteBody);

        RequestBody dorsoBody = RequestBody.create(MediaType.parse("image/*"), fileDorso);
        MultipartBody.Part dorsoPart = MultipartBody.Part.createFormData("imagenDniDorso", fileDorso.getName(), dorsoBody);

        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.finalizarRegistroAlumno(idBody, tramiteBody, frentePart, dorsoPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Registro finalizado con éxito", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_fragment_finalizar_registro_to_nav_inicio);
                } else {
                    Toast.makeText(requireContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}