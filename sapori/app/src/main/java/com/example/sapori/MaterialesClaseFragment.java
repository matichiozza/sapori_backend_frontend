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

import java.util.List;

import com.example.sapori.model.MaterialDeClase;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaterialesClaseFragment extends Fragment {
    private ApiService apiService;
    private RecyclerView recyclerView;
    private MaterialClaseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materiales_clase, container, false);

        ImageView btnCerrar = view.findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView = view.findViewById(R.id.recyclerMateriales);
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

        apiService = ApiClient.getApiService();
        cargarMateriales(cursoId);

        return view;
    }

    private void cargarMateriales(Long cursoId) {
        apiService.getMaterialesPorCurso(cursoId).enqueue(new Callback<List<MaterialDeClase>>() {
            @Override
            public void onResponse(Call<List<MaterialDeClase>> call, Response<List<MaterialDeClase>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MaterialDeClase> materiales = response.body();
                    adapter = new MaterialClaseAdapter(materiales, apiService, getContext());
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No se pudieron obtener los materiales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MaterialDeClase>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al obtener materiales", Toast.LENGTH_SHORT).show();
                Log.e("MaterialesClaseFragment", "Error: ", t);
            }
        });
    }
} 