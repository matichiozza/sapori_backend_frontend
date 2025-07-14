package com.example.demo.datos;

import com.example.demo.modelo.Pago;
import com.example.demo.modelo.Pago.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByCuentaCorrienteId(Long cuentaCorrienteId);

    List<Pago> findByCursoId(Long cursoId);

    List<Pago> findByEstado(EstadoPago estado);
}
