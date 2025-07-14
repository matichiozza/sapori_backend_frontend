package com.example.demo.datos;

import com.example.demo.modelo.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeRepository extends JpaRepository<Sede, Long> {
}
