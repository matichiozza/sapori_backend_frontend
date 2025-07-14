package com.example.demo.datos;

import com.example.demo.modelo.ComentarioValoracion;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ComentarioValoracionDAO {

    @Autowired
    private ComentarioValoracionRepository comentarioValoracionRepository;

    public List<ComentarioValoracion> getAllComentariosValoraciones() {
        return comentarioValoracionRepository.findAll();
    }

    public Optional<ComentarioValoracion> getComentarioValoracionById(Long id) {
        return comentarioValoracionRepository.findById(id);
    }

    public List<ComentarioValoracion> getComentariosValoracionesByRecetaId(Long recetaId) {
        return comentarioValoracionRepository.findByRecetaId(recetaId);
    }

    public List<ComentarioValoracion> getComentariosValoracionesByUsuarioId(Long usuarioId) {
        return comentarioValoracionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public ComentarioValoracion agregarComentarioValoracion(ComentarioValoracion comentarioValoracion) {
        return comentarioValoracionRepository.save(comentarioValoracion);
    }

    @Transactional
    public ComentarioValoracion actualizarComentarioValoracion(ComentarioValoracion comentarioValoracion) {
        if (comentarioValoracionRepository.existsById(comentarioValoracion.getId())) {
            return comentarioValoracionRepository.save(comentarioValoracion);
        }
        return null;
    }

    @Transactional
    public void eliminarComentarioValoracion(Long id) {
        if (comentarioValoracionRepository.existsById(id)) {
            comentarioValoracionRepository.deleteById(id);
        } else {
            throw new RuntimeException("ComentarioValoracion con id " + id + " no encontrado.");
        }
    }
}
