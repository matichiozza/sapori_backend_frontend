package com.example.demo.datos;

import com.example.demo.modelo.Alumno;
import com.example.demo.modelo.Asistencia;
import com.example.demo.modelo.Curso;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AsistenciaDAO {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public List<Asistencia> getAllAsistencias() {
        return asistenciaRepository.findAll();
    }

    public Optional<Asistencia> getAsistenciaById(Long id) {
        return asistenciaRepository.findById(id);
    }

    @Transactional
    public List<Asistencia> getAsistenciasPorAlumno(Long alumnoId) {
        return asistenciaRepository.findByAlumnoIdOrderByFechaAsc(alumnoId);
    }

    @Transactional
    public List<Asistencia> getAsistenciasPorCurso(Long cursoId) {
        return asistenciaRepository.findByCursoIdOrderByFechaAsc(cursoId);
    }

    @Transactional
    public List<Asistencia> getAsistenciasPorAlumnoYCurso(Long alumnoId, Long cursoId) {
        return asistenciaRepository.findByAlumnoIdAndCursoIdOrderByClaseAsc(alumnoId, cursoId);
    }

    @Transactional
    public Asistencia agregarAsistencia(Asistencia asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public Asistencia actualizarAsistencia(Asistencia asistencia) {
        if (asistenciaRepository.existsById(asistencia.getId())) {
            return asistenciaRepository.save(asistencia);
        }
        return null;
    }

    @Transactional
    public void eliminarAsistencia(Long id) {
        if (asistenciaRepository.existsById(id)) {
            asistenciaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Asistencia con id " + id + " no encontrada.");
        }
    }
}
