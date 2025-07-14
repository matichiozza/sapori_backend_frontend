package com.example.demo.datos;

import com.example.demo.modelo.CuentaCorriente;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CuentaCorrienteDAO {

    @Autowired
    private CuentaCorrienteRepository cuentaCorrienteRepository;

    public Optional<CuentaCorriente> getCuentaCorrienteById(Long id) {
        return cuentaCorrienteRepository.findById(id);
    }

    public Optional<CuentaCorriente> getCuentaCorrienteByAlumnoId(Long alumnoId) {
        return cuentaCorrienteRepository.findByAlumnoId(alumnoId);
    }

    @Transactional
    public CuentaCorriente guardarCuentaCorriente(CuentaCorriente cuentaCorriente) {
        cuentaCorriente.recalcularTotal(); // recalculamos el total antes de guardar
        return cuentaCorrienteRepository.save(cuentaCorriente);
    }

    @Transactional
    public CuentaCorriente actualizarCuentaCorriente(CuentaCorriente cuentaCorriente) {
        if (cuentaCorrienteRepository.existsById(cuentaCorriente.getId())) {
            cuentaCorriente.recalcularTotal(); // recalculamos también antes de actualizar
            return cuentaCorrienteRepository.save(cuentaCorriente);
        }
        return null;
    }

    @Transactional
    public void eliminarCuentaCorriente(Long id) {
        if (cuentaCorrienteRepository.existsById(id)) {
            cuentaCorrienteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Cuenta corriente con id " + id + " no encontrada.");
        }
    }
}
