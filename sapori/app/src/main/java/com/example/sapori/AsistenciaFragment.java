package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.sapori.model.Asistencia;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenciaFragment extends Fragment {
    private ApiService apiService;
    private RecyclerView recyclerView;
    private AsistenciaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asistencia, container, false);

        ImageView btnCerrar = view.findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView = view.findViewById(R.id.recyclerAsistencia);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Obtener cursoId del Bundle
        Long cursoId = null;
        if (getArguments() != null && getArguments().containsKey("cursoId")) {
            cursoId = getArguments().getLong("cursoId");
        }
        if (cursoId == null || cursoId == 0) {
            Toast.makeText(getContext(), "No se recibió el cursoId", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Obtener alumnoId de SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        Long alumnoId = prefs.getLong("id_usuario", -1);
        Log.e("DFSSDFFSDFDSSDF", "SADDASDAS" + alumnoId);
        if (alumnoId == -1) {
            Toast.makeText(getContext(), "No se encontró el id del alumno", Toast.LENGTH_SHORT).show();
            return view;
        }

        apiService = ApiClient.getApiService();
        cargarAsistencias(alumnoId, cursoId);

        return view;
    }

    private void cargarAsistencias(Long alumnoId, Long cursoId) {
        apiService.getAsistenciasPorAlumnoYCurso(alumnoId, cursoId).enqueue(new Callback<List<Asistencia>>() {
            @Override
            public void onResponse(Call<List<Asistencia>> call, Response<List<Asistencia>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Asistencia> asistencias = response.body();
                    adapter = new AsistenciaAdapter(asistencias);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No se pudieron obtener las asistencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Asistencia>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al obtener asistencias", Toast.LENGTH_SHORT).show();
                Log.e("AsistenciaFragment", "Error: ", t);
            }
        });
    }

    public static class AsistenciaItem {
        public int clase;
        public String fecha;
        public Boolean presente; // true=presente, false=ausente, null=no cargado
        public AsistenciaItem(int clase, String fecha, Boolean presente) {
            this.clase = clase;
            this.fecha = fecha;
            this.presente = presente;
        }
    }
} 