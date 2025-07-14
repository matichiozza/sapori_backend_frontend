package com.example.demo.datos;

import com.example.demo.modelo.RecetaFavorita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaFavoritaRepository extends JpaRepository<RecetaFavorita, Long> {

    // Buscar recetas favoritas por usuario
    List<RecetaFavorita> findByUsuarioId(Long usuarioId);

    // Buscar una receta favorita específica por usuario y receta (para evitar duplicados)
    RecetaFavorita findByUsuarioIdAndRecetaId(Long usuarioId, Long recetaId);
}
