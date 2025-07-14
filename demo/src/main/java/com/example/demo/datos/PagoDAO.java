package com.example.demo.datos;

import com.example.demo.modelo.Pago;
import com.example.demo.modelo.Pago.EstadoPago;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PagoDAO {

    @Autowired
    private PagoRepository pagoRepository;

    public Optional<Pago> getPagoById(Long id) {
        return pagoRepository.findById(id);
    }

    public List<Pago> getAllPagos() {
        return pagoRepository.findAll();
    }

    public List<Pago> getPagosPorCuentaCorriente(Long cuentaCorrienteId) {
        return pagoRepository.findByCuentaCorrienteId(cuentaCorrienteId);
    }

    public List<Pago> getPagosPorCurso(Long cursoId) {
        return pagoRepository.findByCursoId(cursoId);
    }

    public List<Pago> getPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    @Transactional
    public Pago guardarPago(Pago pago) {
        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago actualizarPago(Pago pago) {
        if (pagoRepository.existsById(pago.getId())) {
            return pagoRepository.save(pago);
        }
        return null;
    }

    @Transactional
    public void eliminarPago(Long id) {
        if (pagoRepository.existsById(id)) {
            pagoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Pago no encontrado con ID: " + id);
        }
    }
}
