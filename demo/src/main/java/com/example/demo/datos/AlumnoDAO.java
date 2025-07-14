package com.example.demo.datos;

import com.example.demo.modelo.Alumno;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AlumnoDAO {

    @Autowired
    private AlumnoRepository alumnoRepository;

    public List<Alumno> getAllAlumnos() {
        return alumnoRepository.findAll();
    }

    public Optional<Alumno> getAlumnoById(Long id) {
        return alumnoRepository.findById(id);
    }

    public Optional<Alumno> getAlumnoConCursosById(Long id) {
        return alumnoRepository.findAlumnoWithCursosById(id);
    }

    @Transactional
    public Alumno agregarAlumno(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    @Transactional
    public Alumno actualizarAlumno(Alumno alumno) {
        if (alumnoRepository.existsById(alumno.getId())) {
            return alumnoRepository.save(alumno);
        }
        return null;
    }

    @Transactional
    public void eliminarAlumno(Long id) {
        if (alumnoRepository.existsById(id)) {
            alumnoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Alumno con id " + id + " no encontrado.");
        }
    }
}