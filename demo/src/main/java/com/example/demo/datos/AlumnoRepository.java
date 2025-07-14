package com.example.demo.datos;

import com.example.demo.modelo.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    @Query("SELECT DISTINCT a FROM Alumno a LEFT JOIN FETCH a.cursosInscriptos WHERE a.id = :id")
    Optional<Alumno> findAlumnoWithCursosById(@Param("id") Long id);

    // Puedes agregar consultas específicas si las necesitas
}
