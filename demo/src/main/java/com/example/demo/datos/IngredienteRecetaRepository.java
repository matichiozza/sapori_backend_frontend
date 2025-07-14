package com.example.demo.datos;

import com.example.demo.modelo.IngredienteReceta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRecetaRepository extends JpaRepository<IngredienteReceta, Long> {

    // Buscar todos los ingredientes por receta
    List<IngredienteReceta> findByRecetaId(Long recetaId);

    // Buscar todos los ingredientes por ingrediente
    List<IngredienteReceta> findByIngredienteId(Long ingredienteId);
}
