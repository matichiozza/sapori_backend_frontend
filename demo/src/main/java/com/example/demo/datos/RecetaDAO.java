package com.example.demo.datos;

import com.example.demo.modelo.Receta;
import com.example.demo.modelo.TipoPlato;
import com.example.demo.modelo.Usuario;

import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecetaDAO {

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;  // <- necesario para buscar usuarios

    public List<Receta> getAllRecetas() {
        return recetaRepository.findAll();
    }

    public Optional<Receta> getRecetaById(Long id) {
        return recetaRepository.findById(id);
    }

    @Transactional
    public List<Receta> getRecetasByNombre(String nombre) {
        return recetaRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc(nombre);
    }

    public List<Receta> getRecetasByUsuarioId(Long usuarioId) {
        return recetaRepository.findByAutorIdOrderByNombreAsc(usuarioId);
    }

    public List<Receta> getRecetasOrdenadasPorFechaDesc() {
        return recetaRepository.findAllByOrderByFechaCreacionDesc();
    }

    public List<Receta> getRecetasOrdenadasPorFechaAsc() {
        return recetaRepository.findAllByOrderByFechaCreacionAsc();
    }

    @Transactional
    public List<Receta> getRecetasByTipos(List<TipoPlato> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            return List.of();  // Lista vacía si no hay tipos
        }
        List<String> tipoNombres = tipos.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return recetaRepository.findByTipoInOrderByNombreAsc(tipoNombres);
    }

    @Transactional
    public List<Receta> getRecetasByNombreYAlias(String nombre, String alias) {
        boolean tieneNombre = nombre != null && !nombre.trim().isEmpty();
        boolean tieneAlias = alias != null && !alias.trim().isEmpty();

        if (tieneNombre && tieneAlias) {
            return recetaRepository.findByNombreContainingIgnoreCaseAndAutorAliasIgnoreCaseOrderByNombreAsc(nombre.trim(), alias.trim());
        } else if (tieneNombre) {
            return recetaRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc(nombre.trim());
        } else if (tieneAlias) {
            return recetaRepository.findByAutorAliasIgnoreCaseOrderByNombreAsc(alias.trim());
        } else {
            return List.of();  // si no hay ni nombre ni alias
        }
    }

    @Transactional
    public List<Receta> getRecetasByAliasUsuario(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return List.of();
        }
        return recetaRepository.findByAutorAliasIgnoreCaseOrderByNombreAsc(alias.trim());
    }

    @Transactional
    public Receta agregarReceta(Receta receta) {
        return recetaRepository.save(receta);
    }

    @Transactional
    public Receta actualizarReceta(Receta receta) {
        if (recetaRepository.existsById(receta.getId())) {
            return recetaRepository.save(receta);
        }
        return null;
    }



    @Transactional
    public void eliminarReceta(Long id) {
        if (recetaRepository.existsById(id)) {
            recetaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Receta con id " + id + " no encontrado.");
        }
    }

    // Nuevo método para obtener favoritas por alias (carga dentro de transacción)
    @Transactional
    public List<Receta> getRecetasFavoritasPorUsuarioId(Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.getRecetasFavoritas().size();  // fuerza carga lazy
            return usuario.getRecetasFavoritas();
        }
        return List.of();
    }

    @Transactional
    public List<Receta> getRecetasConIngredientesMinimos(List<String> nombresIngredientes) {
        if (nombresIngredientes == null || nombresIngredientes.isEmpty()) {
            return List.of();
        }

        // Paso 1: Buscar recetas que tengan alguno de los ingredientes
        List<Receta> recetas = recetaRepository.findDistinctByIngredientes_Ingrediente_NombreIn(nombresIngredientes);

        // Paso 2: Filtrar las recetas que tienen al menos todos los ingredientes requeridos
        return recetas.stream()
                .filter(receta -> {
                    List<String> nombresEnReceta = receta.getIngredientes().stream()
                            .map(ir -> ir.getIngrediente().getNombre())
                            .distinct()
                            .toList();

                    return nombresEnReceta.containsAll(nombresIngredientes);
                })
                .toList();
    }
}
