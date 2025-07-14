package com.example.demo.datos;

import com.example.demo.modelo.PasoReceta;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PasoRecetaDAO {

    @Autowired
    private PasoRecetaRepository pasoRecetaRepository;

    public List<PasoReceta> getAllPasos() {
        return pasoRecetaRepository.findAll();
    }

    public Optional<PasoReceta> getPasoById(Long id) {
        return pasoRecetaRepository.findById(id);
    }

    public List<PasoReceta> getPasosByRecetaId(Long recetaId) {
        return pasoRecetaRepository.findByRecetaIdOrderByNumeroPasoAsc(recetaId);
    }

    @Transactional
    public PasoReceta agregarPaso(PasoReceta pasoReceta) {
        return pasoRecetaRepository.save(pasoReceta);
    }

    @Transactional
    public PasoReceta actualizarPaso(PasoReceta pasoReceta) {
        if (pasoRecetaRepository.existsById(pasoReceta.getId())) {
            return pasoRecetaRepository.save(pasoReceta);
        }
        return null;
    }

    @Transactional
    public void eliminarPaso(Long id) {
        if (pasoRecetaRepository.existsById(id)) {
            pasoRecetaRepository.deleteById(id);
        } else {
            throw new RuntimeException("PasoReceta con id " + id + " no encontrado.");
        }
    }
}
