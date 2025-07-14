package com.example.demo.datos;

import com.example.demo.modelo.PasoReceta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasoRecetaRepository extends JpaRepository<PasoReceta, Long> {

    // Buscar todos los pasos por receta, ordenados por número de paso
    List<PasoReceta> findByRecetaIdOrderByNumeroPasoAsc(Long recetaId);

}
