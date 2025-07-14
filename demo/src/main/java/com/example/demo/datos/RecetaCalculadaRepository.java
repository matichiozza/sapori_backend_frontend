package com.example.demo.datos;

import com.example.demo.modelo.RecetaCalculada;
import com.example.demo.modelo.Receta;
import com.example.demo.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecetaCalculadaRepository extends JpaRepository<RecetaCalculada, Long> {

    List<RecetaCalculada> findByUsuarioId(Long usuarioId);

    Optional<RecetaCalculada> findByRecetaOriginalAndUsuario(Receta recetaOriginal, Usuario usuario);

}
