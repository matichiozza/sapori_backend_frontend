package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapori.model.Pago;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CuentaCorrienteFragment extends Fragment {

    private TextView txtTotal, txtSaldoFavor;
    private LinearLayout layoutSaldoFavor;
    private TabLayout tabLayout;
    private RecyclerView recyclerViewFacturas;
    private FacturaAdapter facturaAdapter;
    private ApiService apiService;
    private List<Pago> facturasAVencer = new ArrayList<>();
    private List<Pago> facturasVencidas = new ArrayList<>();
    private List<Pago> facturasPagas = new ArrayList<>();
    private List<Pago> facturasReembolsadas = new ArrayList<>();

    public CuentaCorrienteFragment() {
        super(R.layout.fragment_cuenta_corriente);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuenta_corriente, container, false);

        // Inicializar vistas
        txtTotal = view.findViewById(R.id.txtTotal);
        txtSaldoFavor = view.findViewById(R.id.txtSaldoFavor);
        layoutSaldoFavor = view.findViewById(R.id.layoutSaldoFavor);
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerViewFacturas = view.findViewById(R.id.recyclerViewFacturas);
        ImageView btnAtras = view.findViewById(R.id.flecha);

        // Configurar RecyclerView
        recyclerViewFacturas.setLayoutManager(new LinearLayoutManager(getContext()));
        facturaAdapter = new FacturaAdapter(new ArrayList<>());
        recyclerViewFacturas.setAdapter(facturaAdapter);

        // Configurar tabs
        tabLayout.addTab(tabLayout.newTab().setText("A pagar"));
        tabLayout.addTab(tabLayout.newTab().setText("Pagas"));
        tabLayout.addTab(tabLayout.newTab().setText("Reembolso"));

        // Configurar listener de tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mostrarFacturasPorTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Obtener datos de cuenta corriente
        cargarCuentaCorriente();

        // Configurar listeners
        btnAtras.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        return view;
    }

    private void cargarCuentaCorriente() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        long usuarioId = prefs.getLong("id_usuario", -1);

        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService = ApiClient.getApiService();
        Call<Map<String, Object>> call = apiService.obtenerCuentaCorriente(usuarioId);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    
                    // Mostrar total
                    Double total = (Double) data.get("total");
                    if (total != null) {
                        txtTotal.setText("$" + String.format("%.2f", total));
                    }

                    // Mostrar saldo a favor si existe
                    Double saldoFavor = (Double) data.get("saldoFavor");
                    if (saldoFavor != null && saldoFavor > 0) {
                        txtSaldoFavor.setText("$" + String.format("%.2f", saldoFavor));
                        layoutSaldoFavor.setVisibility(View.VISIBLE);
                    } else {
                        layoutSaldoFavor.setVisibility(View.GONE);
                    }

                    // Separar facturas por estado
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> aPagar = (List<Map<String, Object>>) data.get("aPagar");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> pagas = (List<Map<String, Object>>) data.get("pagas");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> reembolsados = (List<Map<String, Object>>) data.get("reembolsados");

                    facturasAVencer = convertirMapToPago(aPagar);
                    facturasPagas = convertirMapToPago(pagas);
                    facturasReembolsadas = convertirMapToPago(reembolsados);

                    // Mostrar primer tab por defecto
                    mostrarFacturasPorTab(0);

                    // Guardar cuentaCorrienteId en SharedPreferences si está presente
                    if (data.containsKey("cuentaCorrienteId")) {
                        Long cuentaCorrienteId = null;
                        Object idObj = data.get("cuentaCorrienteId");
                        if (idObj instanceof Number) {
                            cuentaCorrienteId = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            try {
                                cuentaCorrienteId = Long.parseLong((String) idObj);
                            } catch (NumberFormatException ignored) {}
                        }
                        if (cuentaCorrienteId != null) {
                            SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
                            prefs.edit().putLong("cuenta_corriente_id", cuentaCorrienteId).apply();
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar cuenta corriente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Pago> convertirMapToPago(List<Map<String, Object>> facturasMap) {
        List<Pago> facturas = new ArrayList<>();
        if (facturasMap != null) {
            for (Map<String, Object> facturaMap : facturasMap) {
                Pago pago = new Pago();
                pago.setId(((Number) facturaMap.get("id")).longValue());
                pago.setImporte(((Number) facturaMap.get("importe")).floatValue());
                pago.setFechaInicioCurso(java.time.LocalDateTime.parse((String) facturaMap.get("fechaInicioCurso")));
                pago.setEstado(Pago.EstadoPago.valueOf((String) facturaMap.get("estado")));
                
                // Si hay curso asociado
                if (facturaMap.get("curso") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cursoMap = (Map<String, Object>) facturaMap.get("curso");
                    com.example.sapori.model.Curso curso = new com.example.sapori.model.Curso();
                    curso.setId(((Number) cursoMap.get("id")).longValue());
                    curso.setNombre((String) cursoMap.get("nombre"));
                    pago.setCurso(curso);
                }
                
                // Si hay sede asociada
                if (facturaMap.get("sede") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sedeMap = (Map<String, Object>) facturaMap.get("sede");
                    com.example.sapori.model.Sede sede = new com.example.sapori.model.Sede();
                    sede.setId(((Number) sedeMap.get("id")).longValue());
                    sede.setNombre((String) sedeMap.get("nombre"));
                    pago.setSede(sede);
                }
                
                facturas.add(pago);
            }
        }
        return facturas;
    }

    private void mostrarFacturasPorTab(int position) {
        List<Pago> facturasAMostrar = new ArrayList<>();
        
        switch (position) {
            case 0: // A pagar
                facturasAMostrar = facturasAVencer;
                break;
            case 1: // Pagas
                facturasAMostrar = facturasPagas;
                break;
            case 2: // Reembolsados
                facturasAMostrar = facturasReembolsadas;
                break;
        }
        
        facturaAdapter.actualizarFacturas(facturasAMostrar);
    }
} 