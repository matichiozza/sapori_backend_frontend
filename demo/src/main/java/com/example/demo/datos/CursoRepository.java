package com.example.demo.datos;

import com.example.demo.modelo.Curso;
import com.example.demo.modelo.Curso.EstadoCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    List<Curso> findByEstadoCursoOrderByFechaInicioAsc(EstadoCurso estadoCurso);
}
