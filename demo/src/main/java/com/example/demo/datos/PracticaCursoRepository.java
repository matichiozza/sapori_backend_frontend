package com.example.demo.datos;

import com.example.demo.modelo.PracticaCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticaCursoRepository extends JpaRepository<PracticaCurso, Long> {

    List<PracticaCurso> findByCursoId(Long cursoId);
}
