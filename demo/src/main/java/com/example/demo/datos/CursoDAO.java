package com.example.demo.datos;

import com.example.demo.modelo.Curso;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CursoDAO {

    @Autowired
    private CursoRepository cursoRepository;

    public List<Curso> getAllCursos() {
        return cursoRepository.findAll();
    }

    public Optional<Curso> getCursoById(Long id) {
        return cursoRepository.findById(id);
    }

    @Transactional
    public List<Curso> getCursosPorNombre(String nombre) {
        return cursoRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc(nombre);
    }


    @Transactional
    public Curso agregarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Transactional
    public Curso actualizarCurso(Curso curso) {
        if (cursoRepository.existsById(curso.getId())) {
            return cursoRepository.save(curso);
        }
        return null;
    }

    @Transactional
    public void eliminarCurso(Long id) {
        if (cursoRepository.existsById(id)) {
            cursoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Curso con id " + id + " no encontrado.");
        }
    }

    @Transactional
    public List<Curso> getCursosPorEstado(Curso.EstadoCurso estadoCurso) {
        return cursoRepository.findByEstadoCursoOrderByFechaInicioAsc(estadoCurso);
    }
}
