package com.example.demo.datos;

import com.example.demo.modelo.MaterialDeClase;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MaterialDeClaseDAO {

    @Autowired
    private MaterialDeClaseRepository materialRepository;

    public Optional<MaterialDeClase> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    public List<MaterialDeClase> getAllMateriales() {
        return materialRepository.findAll();
    }

    public List<MaterialDeClase> getMaterialesPorCurso(Long cursoId) {
        return materialRepository.findByCursoId(cursoId);
    }

    @Transactional
    public MaterialDeClase guardarMaterial(MaterialDeClase material) {
        return materialRepository.save(material);
    }

    @Transactional
    public MaterialDeClase actualizarMaterial(MaterialDeClase material) {
        if (materialRepository.existsById(material.getId())) {
            return materialRepository.save(material);
        }
        return null;
    }

    @Transactional
    public void eliminarMaterial(Long id) {
        if (materialRepository.existsById(id)) {
            materialRepository.deleteById(id);
        } else {
            throw new RuntimeException("Material no encontrado con ID: " + id);
        }
    }
}
