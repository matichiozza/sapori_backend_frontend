package com.example.demo.datos;

import com.example.demo.modelo.Ingrediente;
import com.example.demo.datos.IngredienteRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IngredienteDAO {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    public List<Ingrediente> getAllIngredientes() {
        return ingredienteRepository.findAll();
    }

    public Optional<Ingrediente> getIngredienteById(Long id) {
        return ingredienteRepository.findById(id);
    }

    @Transactional
    public Ingrediente agregarIngrediente(Ingrediente ingrediente) {
        return ingredienteRepository.save(ingrediente);
    }

    @Transactional
    public Ingrediente actualizarIngrediente(Ingrediente ingrediente) {
        if (ingrediente.getId() == null) {
            throw new IllegalArgumentException("El ID del ingrediente no puede ser nulo para actualizar.");
        }
        if (ingredienteRepository.existsById(ingrediente.getId())) {
            return ingredienteRepository.save(ingrediente);
        } else {
            throw new RuntimeException("Ingrediente con id " + ingrediente.getId() + " no encontrado para actualizar.");
        }
    }

    @Transactional
    public void eliminarIngrediente(Long id) {
        if (ingredienteRepository.existsById(id)) {
            ingredienteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ingrediente con id " + id + " no encontrado.");
        }
    }

    // Actualiza la URL de imagen por nombre de ingrediente (asegúrate que 'findByNombre' esté en el repo)
    @Transactional
    public Ingrediente actualizarImagenUrlPorNombre(String nombre, String urlImagen) {
        Optional<Ingrediente> optIngrediente = ingredienteRepository.findByNombre(nombre);
        if (optIngrediente.isPresent()) {
            Ingrediente ingrediente = optIngrediente.get();
            ingrediente.setImagenUrl(urlImagen);
            return ingredienteRepository.save(ingrediente);
        } else {
            throw new RuntimeException("Ingrediente con nombre '" + nombre + "' no encontrado.");
        }
    }

    @Transactional
    public void borrarTodasLasImagenes() {
        List<Ingrediente> ingredientes = ingredienteRepository.findAll();
        for (Ingrediente ing : ingredientes) {
            ing.setImagenUrl(null);
            ingredienteRepository.save(ing);
        }
    }
}