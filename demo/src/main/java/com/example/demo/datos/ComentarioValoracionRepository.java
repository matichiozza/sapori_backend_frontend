package com.example.demo.datos;

import com.example.demo.modelo.ComentarioValoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioValoracionRepository extends JpaRepository<ComentarioValoracion, Long> {

    // Buscar comentarios/valoraciones por receta
    List<ComentarioValoracion> findByRecetaId(Long recetaId);

    // Buscar comentarios/valoraciones por usuario
    List<ComentarioValoracion> findByUsuarioId(Long usuarioId);

}
