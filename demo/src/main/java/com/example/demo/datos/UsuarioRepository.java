package com.example.demo.datos;

import com.example.demo.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByAlias(String alias);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByAlias(String alias);
}
