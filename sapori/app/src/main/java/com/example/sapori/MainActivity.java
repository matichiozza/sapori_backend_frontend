package com.example.sapori;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private LinearLayout navInicio, navRecetas, navCursos, navAjustes;

    private View lineaInicio, lineaRecetas, lineaCursos, lineaAjustes;
    private ImageView iconInicio, iconRecetas, iconCursos, iconAjustes;

    // Agrega las referencias a los TextView
    private TextView textInicio, textRecetas, textCursos, textAjustes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navInicio = findViewById(R.id.nav_inicio);
        navRecetas = findViewById(R.id.nav_recetas);
        navCursos = findViewById(R.id.nav_cursos);
        navAjustes = findViewById(R.id.nav_ajustes);

        lineaInicio = findViewById(R.id.linea_inicio);
        lineaRecetas = findViewById(R.id.linea_recetas);
        lineaCursos = findViewById(R.id.linea_cursos);
        lineaAjustes = findViewById(R.id.linea_ajustes);

        iconInicio = findViewById(R.id.icon_inicio);
        iconRecetas = findViewById(R.id.icon_recetas);
        iconCursos = findViewById(R.id.icon_cursos);
        iconAjustes = findViewById(R.id.icon_ajustes);

        // Inicializar los TextView
        textInicio = findViewById(R.id.text_inicio);
        textRecetas = findViewById(R.id.text_recetas);
        textCursos = findViewById(R.id.text_cursos);
        textAjustes = findViewById(R.id.text_ajustes);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            throw new IllegalStateException("No se encontró NavHostFragment");
        }

        navController = navHostFragment.getNavController();

        navInicio.setOnClickListener(v -> navController.navigate(R.id.nav_inicio));
        navRecetas.setOnClickListener(v -> navController.navigate(R.id.nav_recetas));
        navCursos.setOnClickListener(v -> navController.navigate(R.id.nav_cursos));
        navAjustes.setOnClickListener(v -> navController.navigate(R.id.nav_ajustes));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            actualizarSeleccion(destination.getId());
        });
    }

    private void actualizarSeleccion(int destinoId) {
        int colorActivo = ContextCompat.getColor(this, R.color.verde_activo);
        int colorInactivo = ContextCompat.getColor(this, R.color.gris_inactivo); // define un gris para el texto inactivo

        // Limpiar filtros iconos
        iconInicio.clearColorFilter();
        iconRecetas.clearColorFilter();
        iconCursos.clearColorFilter();
        iconAjustes.clearColorFilter();

        // Reset color texto
        textInicio.setTextColor(colorInactivo);
        textRecetas.setTextColor(colorInactivo);
        textCursos.setTextColor(colorInactivo);
        textAjustes.setTextColor(colorInactivo);

        // Ocultar todas las líneas
        lineaInicio.setVisibility(View.GONE);
        lineaRecetas.setVisibility(View.GONE);
        lineaCursos.setVisibility(View.GONE);
        lineaAjustes.setVisibility(View.GONE);

        if (destinoId == R.id.nav_inicio) {
            iconInicio.setColorFilter(colorActivo);
            textInicio.setTextColor(colorActivo);
            lineaInicio.setVisibility(View.VISIBLE);

        } else if (destinoId == R.id.nav_recetas ||
                destinoId == R.id.nav_buscar_recetas ||
                destinoId == R.id.nav_recetas_calculadas ||
                destinoId == R.id.nav_buscar_recetas_favoritas ||
                destinoId == R.id.nav_gestionar_recetas ||
                destinoId == R.id.agregarRecetaFragment ||
                destinoId == R.id.fragment_detalle_receta ||
                destinoId == R.id.fragment_filtrar_receta) {
            iconRecetas.setColorFilter(colorActivo);
            textRecetas.setTextColor(colorActivo);
            lineaRecetas.setVisibility(View.VISIBLE);

        } else if (destinoId == R.id.nav_cursos) {
            iconCursos.setColorFilter(colorActivo);
            textCursos.setTextColor(colorActivo);
            lineaCursos.setVisibility(View.VISIBLE);

        } else if (destinoId == R.id.nav_ajustes ||
                destinoId == R.id.nav_datos_personales ||
                destinoId == R.id.fragment_recuperar_contrasenia_1 ||
                destinoId == R.id.fragment_recuperar_contrasenia_2 ||
                destinoId == R.id.fragment_recuperar_contrasenia_3) {
            iconAjustes.setColorFilter(colorActivo);
            textAjustes.setTextColor(colorActivo);
            lineaAjustes.setVisibility(View.VISIBLE);
        }
    }
}
