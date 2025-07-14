package com.example.demo.datos;

import com.example.demo.modelo.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByAlumnoIdOrderByFechaAsc(Long alumnoId);

    List<Asistencia> findByCursoIdOrderByFechaAsc(Long cursoId);

    List<Asistencia> findByAlumnoIdAndCursoIdOrderByClaseAsc(Long alumnoId, Long cursoId);
}
