package com.example.demo.datos;

import com.example.demo.modelo.Ingrediente;
import com.example.demo.modelo.IngredienteReceta;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IngredienteRecetaDAO {

    @Autowired
    private IngredienteRecetaRepository ingredienteRecetaRepository;

    public List<IngredienteReceta> getAllIngredienteRecetas() {
        return ingredienteRecetaRepository.findAll();
    }

    public Optional<IngredienteReceta> getIngredienteRecetaById(Long id) {
        return ingredienteRecetaRepository.findById(id);
    }

    public List<IngredienteReceta> getIngredienteRecetasByRecetaId(Long recetaId) {
        return ingredienteRecetaRepository.findByRecetaId(recetaId);
    }

    public List<Ingrediente> getIngredientesDeReceta(Long recetaId) {
        List<IngredienteReceta> ingredientesReceta = ingredienteRecetaRepository.findByRecetaId(recetaId);
        return ingredientesReceta.stream()
                .map(IngredienteReceta::getIngrediente)
                .distinct() // evita duplicados si hay
                .toList();
    }



    public List<IngredienteReceta> getIngredienteRecetasByIngredienteId(Long ingredienteId) {
        return ingredienteRecetaRepository.findByIngredienteId(ingredienteId);
    }



    @Transactional
    public IngredienteReceta agregarIngredienteReceta(IngredienteReceta ingredienteReceta) {
        return ingredienteRecetaRepository.save(ingredienteReceta);
    }

    @Transactional
    public IngredienteReceta actualizarIngredienteReceta(IngredienteReceta ingredienteReceta) {
        if (ingredienteRecetaRepository.existsById(ingredienteReceta.getId())) {
            return ingredienteRecetaRepository.save(ingredienteReceta);
        }
        return null;
    }

    @Transactional
    public void eliminarIngredienteReceta(Long id) {
        if (ingredienteRecetaRepository.existsById(id)) {
            ingredienteRecetaRepository.deleteById(id);
        } else {
            throw new RuntimeException("IngredienteReceta con id " + id + " no encontrado.");
        }
    }
}
