package com.example.demo.datos;

import com.example.demo.modelo.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    // Podés agregar métodos custom si los necesitás, por ejemplo:
    Optional<Ingrediente> findByNombre(String nombre);

    @Modifying
    @Query("UPDATE Ingrediente i SET i.imagenUrl = null")
    void borrarTodasLasImagenes();

    Optional<Ingrediente> findByNombreIgnoreCase(String nombre);

}
