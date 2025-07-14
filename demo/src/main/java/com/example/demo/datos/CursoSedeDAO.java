package com.example.demo.datos;

import com.example.demo.modelo.CursoSede;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CursoSedeDAO {

    @Autowired
    private CursoSedeRepository cursoSedeRepository;

    public Optional<CursoSede> getCursoSedeById(Long id) {
        return cursoSedeRepository.findById(id);
    }

    public List<CursoSede> getAllCursoSede() {
        return cursoSedeRepository.findAll();
    }

    public List<CursoSede> getByCursoId(Long cursoId) {
        return cursoSedeRepository.findByCursoId(cursoId);
    }

    public List<CursoSede> getBySedeId(Long sedeId) {
        return cursoSedeRepository.findBySedeId(sedeId);
    }

    public Optional<CursoSede> getByCursoIdAndSedeId(Long cursoId, Long sedeId) {
        return cursoSedeRepository.findByCursoIdAndSedeId(cursoId, sedeId);
    }

    @Transactional
    public CursoSede guardarCursoSede(CursoSede cursoSede) {
        cursoSede.actualizarVacantesDisponibles(); // opcional: recalcular
        return cursoSedeRepository.save(cursoSede);
    }

    @Transactional
    public CursoSede actualizarCursoSede(CursoSede cursoSede) {
        if (cursoSedeRepository.existsById(cursoSede.getId())) {
            cursoSede.actualizarVacantesDisponibles(); // opcional: recalcular
            return cursoSedeRepository.save(cursoSede);
        }
        return null;
    }

    @Transactional
    public void eliminarCursoSede(Long id) {
        if (cursoSedeRepository.existsById(id)) {
            cursoSedeRepository.deleteById(id);
        } else {
            throw new RuntimeException("CursoSede con id " + id + " no encontrado.");
        }
    }
}
