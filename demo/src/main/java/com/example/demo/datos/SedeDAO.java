package com.example.demo.datos;

import com.example.demo.modelo.Sede;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SedeDAO {

    @Autowired
    private SedeRepository sedeRepository;

    public Optional<Sede> getSedeById(Long id) {
        return sedeRepository.findById(id);
    }

    public List<Sede> getAllSedes() {
        return sedeRepository.findAll();
    }

    @Transactional
    public Sede guardarSede(Sede sede) {
        return sedeRepository.save(sede);
    }

    @Transactional
    public Sede actualizarSede(Sede sede) {
        if (sedeRepository.existsById(sede.getId())) {
            return sedeRepository.save(sede);
        }
        return null;
    }

    @Transactional
    public void eliminarSede(Long id) {
        if (sedeRepository.existsById(id)) {
            sedeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Sede no encontrada con ID: " + id);
        }
    }
}
