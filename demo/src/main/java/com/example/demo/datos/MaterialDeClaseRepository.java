package com.example.demo.datos;

import com.example.demo.modelo.MaterialDeClase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialDeClaseRepository extends JpaRepository<MaterialDeClase, Long> {
    List<MaterialDeClase> findByCursoId(Long cursoId);
}
