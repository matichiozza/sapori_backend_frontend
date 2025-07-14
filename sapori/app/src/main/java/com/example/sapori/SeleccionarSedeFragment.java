package com.example.sapori;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Curso;
import com.example.sapori.model.CursoSede;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeleccionarSedeFragment extends Fragment {

    private RecyclerView recyclerViewSedes;
    private SedeAdapter sedeAdapter;
    private Long cursoId;
    private static final String TAG = "SeleccionarSedeFragment";
    private Curso curso;

    public SeleccionarSedeFragment() {
        super(R.layout.fragment_seleccionar_sede);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cursoId = getArguments().getLong("cursoId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seleccionar_sede, container, false);
        
        // Configurar RecyclerView
        recyclerViewSedes = view.findViewById(R.id.recyclerViewSedes);
        recyclerViewSedes.setLayoutManager(new LinearLayoutManager(getContext()));
        sedeAdapter = new SedeAdapter(new ArrayList<>());
        
        // Configurar listeners del adapter
        sedeAdapter.setOnSedeSelectedListener((sede, position) -> {
            Log.d(TAG, "Sede seleccionada: " + sede.getSede().getNombre());
        });
        
        sedeAdapter.setOnContinuarClickListener(() -> {
            CursoSede sedeSeleccionada = sedeAdapter.getSedeSeleccionada();
            if (sedeSeleccionada != null) {
                if (curso == null) {
                    Toast.makeText(requireContext(), "No se pudo obtener el curso", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("curso", curso);
                bundle.putSerializable("sede", sedeSeleccionada);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_seleccionarSedeFragment_to_pagoFragment, bundle);
            } else {
                Toast.makeText(requireContext(), "Debes seleccionar una sede", Toast.LENGTH_SHORT).show();
            }
        });
        
        sedeAdapter.setOnVerDetalleClickListener((sede) -> {
            // Obtener la URL de la imagen de la sede
            String urlImagenSede = null;
            if (sede.getSede().getFotosUrlSedes() != null && !sede.getSede().getFotosUrlSedes().isEmpty()) {
                urlImagenSede = sede.getSede().getFotosUrlSedes().get(0);
            }
            
            // Mostrar el popup con el detalle de la sede
            DetalleSedeDialog dialog = new DetalleSedeDialog(requireContext(), sede.getSede().getId(), urlImagenSede);
            dialog.show();
        });
        
        recyclerViewSedes.setAdapter(sedeAdapter);
        
        // Configurar flecha de regreso
        ImageView btnAtras = view.findViewById(R.id.btnAtras);
        btnAtras.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });
        
        cargarSedes();
        return view;
    }

    private void cargarSedes() {
        if (cursoId == null || cursoId == -1) {
            Toast.makeText(requireContext(), "Error: ID de curso no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ApiService apiService = ApiClient.getApiService();
        Call<List<CursoSede>> call = apiService.obtenerCursoSedesCompletos(cursoId);
        call.enqueue(new Callback<List<CursoSede>>() {
            @Override
            public void onResponse(Call<List<CursoSede>> call, Response<List<CursoSede>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Sedes cargadas: " + response.body().size());
                    sedeAdapter.actualizarSedes(response.body());
                    curso = response.body().get(0).getCurso();
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar las sedes", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<CursoSede>> call, Throwable t) {
                Log.e(TAG, "Error de conexión", t);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 