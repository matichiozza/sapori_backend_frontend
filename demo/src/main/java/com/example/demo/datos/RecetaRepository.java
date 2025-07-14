package com.example.demo.datos;

import com.example.demo.modelo.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    // Buscar recetas por nombre (contiene texto, case insensitive)
    List<Receta> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    // Buscar recetas por usuario id
    List<Receta> findByAutorIdOrderByNombreAsc(Long usuarioId);

    List<Receta> findByUsuariosQueLaFavoritaron_Id(Long usuarioId);

    List<Receta> findByAutorAliasIgnoreCaseOrderByNombreAsc(String alias);

    List<Receta> findByTipoInOrderByNombreAsc(List<String> tipos);

    List<Receta> findAllByOrderByFechaCreacionDesc();

    List<Receta> findAllByOrderByFechaCreacionAsc();

    List<Receta> findDistinctByIngredientes_Ingrediente_NombreIn(List<String> nombres);

    List<Receta> findByNombreContainingIgnoreCaseAndAutorAliasIgnoreCaseOrderByNombreAsc(String nombre, String alias);



}
