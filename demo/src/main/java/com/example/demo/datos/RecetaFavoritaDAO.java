package com.example.demo.datos;

import com.example.demo.modelo.RecetaFavorita;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecetaFavoritaDAO {

    @Autowired
    private RecetaFavoritaRepository recetaFavoritaRepository;

    public List<RecetaFavorita> getAllRecetasFavoritas() {
        return recetaFavoritaRepository.findAll();
    }

    public Optional<RecetaFavorita> getRecetaFavoritaById(Long id) {
        return recetaFavoritaRepository.findById(id);
    }

    public List<RecetaFavorita> getRecetasFavoritasByUsuarioId(Long usuarioId) {
        return recetaFavoritaRepository.findByUsuarioId(usuarioId);
    }

    public RecetaFavorita getRecetaFavoritaByUsuarioIdAndRecetaId(Long usuarioId, Long recetaId) {
        return recetaFavoritaRepository.findByUsuarioIdAndRecetaId(usuarioId, recetaId);
    }

    @Transactional
    public RecetaFavorita agregarRecetaFavorita(RecetaFavorita recetaFavorita) {
        return recetaFavoritaRepository.save(recetaFavorita);
    }

    @Transactional
    public RecetaFavorita actualizarRecetaFavorita(RecetaFavorita recetaFavorita) {
        if (recetaFavoritaRepository.existsById(recetaFavorita.getId())) {
            return recetaFavoritaRepository.save(recetaFavorita);
        }
        return null;
    }

    @Transactional
    public void eliminarRecetaFavorita(Long id) {
        if (recetaFavoritaRepository.existsById(id)) {
            recetaFavoritaRepository.deleteById(id);
        } else {
            throw new RuntimeException("RecetaFavorita con id " + id + " no encontrado.");
        }
    }
}
