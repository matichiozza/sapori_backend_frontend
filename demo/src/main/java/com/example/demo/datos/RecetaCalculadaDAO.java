package com.example.demo.datos;

import com.example.demo.modelo.RecetaCalculada;
import com.example.demo.modelo.Receta;
import com.example.demo.modelo.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RecetaCalculadaDAO {

    @Autowired
    private RecetaCalculadaRepository recetaCalculadaRepository;

    public List<RecetaCalculada> getRecetasCalculadasByUsuarioId(Long usuarioId) {
        return recetaCalculadaRepository.findByUsuarioId(usuarioId);
    }

    public Optional<RecetaCalculada> getRecetaCalculadaById(Long id) {
        return recetaCalculadaRepository.findById(id);
    }

    @Transactional
    public RecetaCalculada guardarRecetaCalculada(RecetaCalculada receta) {
        return recetaCalculadaRepository.save(receta);
    }

    public Optional<RecetaCalculada> buscarPorRecetaYUsuario(Receta receta, Usuario usuario) {
        return recetaCalculadaRepository.findByRecetaOriginalAndUsuario(receta, usuario);
    }

    @Transactional
    public void borrarRecetaCalculada(RecetaCalculada recetaCalculada) {
        recetaCalculadaRepository.delete(recetaCalculada);
    }

    public List<String> getIngredientesJsonPorUsuarioId(Long usuarioId) {
        List<RecetaCalculada> recetas = recetaCalculadaRepository.findByUsuarioId(usuarioId);
        List<String> ingredientesJsonList = new ArrayList<>();

        for (RecetaCalculada receta : recetas) {
            String json = receta.getIngredientesEscaladosJson();
            if (json != null && !json.isEmpty()) {
                ingredientesJsonList.add(json);
            }
        }

        return ingredientesJsonList;
    }


}
