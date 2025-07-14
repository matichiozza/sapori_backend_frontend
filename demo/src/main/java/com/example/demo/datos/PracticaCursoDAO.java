package com.example.demo.datos;

import com.example.demo.modelo.PracticaCurso;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PracticaCursoDAO {

    @Autowired
    private PracticaCursoRepository practicaCursoRepository;

    public Optional<PracticaCurso> getPracticaCursoById(Long id) {
        return practicaCursoRepository.findById(id);
    }

    public List<PracticaCurso> getAllPracticas() {
        return practicaCursoRepository.findAll();
    }

    public List<PracticaCurso> getPracticasPorCurso(Long cursoId) {
        return practicaCursoRepository.findByCursoId(cursoId);
    }

    @Transactional
    public PracticaCurso guardarPractica(PracticaCurso practica) {
        return practicaCursoRepository.save(practica);
    }

    @Transactional
    public PracticaCurso actualizarPractica(PracticaCurso practica) {
        if (practicaCursoRepository.existsById(practica.getId())) {
            return practicaCursoRepository.save(practica);
        }
        return null;
    }

    @Transactional
    public void eliminarPractica(Long id) {
        if (practicaCursoRepository.existsById(id)) {
            practicaCursoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Práctica no encontrada con ID: " + id);
        }
    }
}
