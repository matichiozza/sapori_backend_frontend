package com.example.demo.datos;

import com.example.demo.modelo.CursoSede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursoSedeRepository extends JpaRepository<CursoSede, Long> {
    List<CursoSede> findByCursoId(Long cursoId);
    List<CursoSede> findBySedeId(Long sedeId);
    Optional<CursoSede> findByCursoIdAndSedeId(Long cursoId, Long sedeId);
}
