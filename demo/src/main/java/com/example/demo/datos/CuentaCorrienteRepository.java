package com.example.demo.datos;

import com.example.demo.modelo.CuentaCorriente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, Long> {
    Optional<CuentaCorriente> findByAlumnoId(Long alumnoId);
}
