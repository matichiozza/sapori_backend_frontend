package com.example.sapori;


import static com.example.sapori.BuscarRecetasFragment.encodeURL;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private static final String TAG = "InicioFragment";

    private TextView txtBienvenida;
    private LinearLayout tarjetaContainer;
    private View grupoInicioSesion; // Agrupar los botones o vistas para cuando no está logueado
    private View contenidoNormal;   // El contenido visible normalmente (ej. últimas recetas)

    public InicioFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: verificando conexión...");
        if (!hayConexion(requireContext())) {
            Log.d(TAG, "No hay conexión, inflando fragment_sin_conexion");
            return inflater.inflate(R.layout.fragment_sin_conexion, container, false);
        } else {
            Log.d(TAG, "Hay conexión, inflando fragment_inicio");
            return inflater.inflate(R.layout.fragment_inicio, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: configurando UI");

        txtBienvenida = view.findViewById(R.id.txt_bienvenida);
        grupoInicioSesion = view.findViewById(R.id.grupo_inicio_sesion);  // Asegurate de que exista este grupo en tu XML (puede ser LinearLayout o similar)
        contenidoNormal = view.findViewById(R.id.contenido_normal);        // Lo mismo, el contenido que siempre mostrás (por ej. recetas)
        tarjetaContainer = view.findViewById(R.id.tarjeta_container);

        if (tarjetaContainer != null) {
            cargarRecetas(); // llamada al método que traerá las recetas
        }

        configurarBotones(view);

        // Manejo visibilidad Bottom Navigation según conexión
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(hayConexion(requireContext()) ? View.VISIBLE : View.GONE);
        }

        // Botón reintentar conexión (solo si está en layout sin conexión)
        View retryBtn = view.findViewById(R.id.btn_reintentar);
        if (retryBtn != null) {
            retryBtn.setOnClickListener(v -> {
                Log.d(TAG, "Click en btn_reintentar");
                if (hayConexion(requireContext())) {
                    try {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.nav_inicio);
                        Log.d(TAG, "Navegó a nav_inicio");
                    } catch (Exception e) {
                        Log.e(TAG, "Error navegando en btn_reintentar", e);
                        Toast.makeText(requireContext(), "Error al navegar, intenta de nuevo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "No hay conexión todavía", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Actualizar UI según estado de login cada vez que el fragmento aparece
        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        boolean logueado = prefs.getBoolean("logueado", false);

        Log.d(TAG, "onResume - logueado: " + logueado);

        if (logueado) {
            long idUsuario = prefs.getLong("id_usuario", -1);
            if (grupoInicioSesion != null) {
                grupoInicioSesion.setVisibility(View.GONE);
            }
            if (contenidoNormal != null) {
                contenidoNormal.setVisibility(View.VISIBLE);
            }
            if (idUsuario != -1) {
                ApiService apiService = ApiClient.getApiService();
                apiService.obtenerUsuarioPorId(idUsuario).enqueue(new Callback<Usuario>() {
                    @Override
                    public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Usuario usuario = response.body();
                            String nombre = usuario.getNombre(); // o getAlias(), según tu modelo
                            boolean esAlumno = usuario.isEsAlumno(); // asegurate de tener este getter

                            if (txtBienvenida != null) {
                                if (esAlumno) {
                                    txtBienvenida.setText("¡Alumno " + nombre + "!");
                                } else {
                                    txtBienvenida.setText("¡Bienvenido " + nombre + "!");
                                }
                                txtBienvenida.setVisibility(View.VISIBLE);
                            }
                        } else {
                            txtBienvenida.setText("¡Bienvenido!");
                        }
                    }

                    @Override
                    public void onFailure(Call<Usuario> call, Throwable t) {
                        txtBienvenida.setText("¡Bienvenido!");
                    }
                });
            }
        }

    }

    private void configurarBotones(View view) {
        // Registro
        View txtRegistro = view.findViewById(R.id.txt_registro);
        if (txtRegistro != null) {
            txtRegistro.setOnClickListener(v -> {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_nav_inicio_to_nav_registro);
                    Log.d(TAG, "Navegó a registro");
                } catch (Exception e) {
                    Log.e(TAG, "Error navegando a registro", e);
                    Toast.makeText(requireContext(), "Error al navegar a registro", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Iniciar Sesión
        View btnIniciarSesion = view.findViewById(R.id.btn_iniciar_sesion);
        if (btnIniciarSesion != null) {
            btnIniciarSesion.setOnClickListener(v -> {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_nav_inicio_to_nav_login);
                    Log.d(TAG, "Navegó a login");
                } catch (Exception e) {
                    Log.e(TAG, "Error navegando a login", e);
                    Toast.makeText(requireContext(), "Error al navegar a login", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean hayConexion(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean conectado = activeNetwork != null && activeNetwork.isConnected();
            Log.d(TAG, "hayConexion: " + conectado);
            return conectado;
        }
        Log.d(TAG, "hayConexion: ConnectivityManager es null");
        return false;
    }

    private void cargarRecetas() {
        ApiService apiService = ApiClient.getApiService();

        Call<List<Map<String, Object>>> call = apiService.obtenerRecetasOrdenadasPorFechaDesc();

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarRecetas(response.body());
                } else {
                    Toast.makeText(requireContext(), "Error al obtener recetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarRecetas(List<Map<String, Object>> recetas) {
        tarjetaContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        int count = 0;
        for (Map<String, Object> receta : recetas) {
            if (count >= 3) break;
            count++;
            View tarjeta = inflater.inflate(R.layout.fragment_tarjeta_receta, tarjetaContainer, false);

            TextView txtNombre = tarjeta.findViewById(R.id.txt_nombre);
            TextView txtAutor = tarjeta.findViewById(R.id.nombreautor);
            TextView txtCalificacion = tarjeta.findViewById(R.id.calificacion);
            TextView txtPorciones = tarjeta.findViewById(R.id.txt_porciones);
            TextView txtTiempo = tarjeta.findViewById(R.id.tiempo);
            TextView txtFecha = tarjeta.findViewById(R.id.txt_fecha);
            ImageView imagenReceta = tarjeta.findViewById(R.id.imagen_receta);

            // Si en el layout fragment_tarjeta_receta hay un botón favorito, ocultarlo:
            View btnFavorito = tarjeta.findViewById(R.id.btn_favorito);
            if (btnFavorito != null) {
                btnFavorito.setVisibility(View.GONE);  // Ocultar botón de favoritos
            }

            View botonVerde = tarjeta.findViewById(R.id.botonVerde);
            if (botonVerde != null) {
                botonVerde.setVisibility(View.GONE);  // Ocultar botón de favoritos
            }

            String nombre = safeCastString(receta.get("nombre"), "Sin nombre");
            String autor = "Autor desconocido";
            Object autorObj = receta.get("autor");
            if (autorObj instanceof Map) {
                Map<?, ?> autorMap = (Map<?, ?>) autorObj;
                Object aliasObj = autorMap.get("alias");  // Cambia "alias" si tu campo tiene otro nombre
                if (aliasObj != null) {
                    autor = aliasObj.toString();
                }
            } else if (autorObj != null) {
                autor = autorObj.toString();
            }
            String calificacion = safeCastString(receta.get("calificacion"), "N/D");
            String fotoPrincipal = safeCastString(receta.get("fotoPrincipal"), "");
            String fecha = safeCastString(receta.get("fecha"), "");
            String idReceta = String.valueOf(receta.get("id"));

            txtNombre.setText(nombre);
            txtAutor.setText(autor);
            txtCalificacion.setText(calificacion);
            txtPorciones.setText(parseIntString(receta.get("porciones"), "N/D"));

            String tiempoStr = parseIntString(receta.get("tiempo"), "N/D");
            txtTiempo.setText(tiempoStr.equals("N/D") ? tiempoStr : tiempoStr + "'");

            txtFecha.setText(fecha.isEmpty() ? "Fecha no disponible" : fecha);

            if (!fotoPrincipal.isEmpty()) {
                Glide.with(requireContext())
                        .load(encodeURL(fotoPrincipal))
                        .centerCrop()
                        .placeholder(R.drawable.imagen_default)
                        .error(R.drawable.imagen_default)
                        .into(imagenReceta);
            } else {
                imagenReceta.setImageResource(R.drawable.imagen_default);
            }

            Object idObj = receta.get("id");
            if (idObj != null) {
                long recetaId;
                try {
                    recetaId = idObj instanceof Number ? ((Number) idObj).longValue() : (long) Double.parseDouble(idObj.toString());

                    long finalRecetaId = recetaId;
                    tarjeta.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putLong("recetaId", finalRecetaId);
                        Navigation.findNavController(v).navigate(R.id.action_nav_inicio_to_detalleRecetaFragment, bundle);
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "ID de receta inválido", Toast.LENGTH_SHORT).show();
                }
            }

            tarjetaContainer.addView(tarjeta);
        }
    }

    private String safeCastString(Object obj, String defaultValue) {
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj != null) {
            return obj.toString();
        } else {
            return defaultValue;
        }
    }

    private String parseIntString(Object obj, String defaultValue) {
        try {
            if (obj instanceof Number) {
                return String.valueOf(((Number) obj).intValue());
            } else if (obj instanceof String) {
                return String.valueOf((int) Double.parseDouble((String) obj));
            } else if (obj != null) {
                return String.valueOf((int) Double.parseDouble(obj.toString()));
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }



}