package com.example.demo.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recetas_calculadas")
public class RecetaCalculada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta recetaOriginal;

    private Integer porciones;

    @Column(name = "tipo_calculado")
    private Boolean tipoCalculado = Boolean.FALSE;

    private String nombre;

    @Column(length = 10000)
    private String ingredientesEscaladosJson;

    private LocalDateTime fechaCreacion;

    public RecetaCalculada() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Receta getRecetaOriginal() {
        return recetaOriginal;
    }

    public void setRecetaOriginal(Receta recetaOriginal) {
        this.recetaOriginal = recetaOriginal;
    }

    public Integer getPorciones() {
        return porciones;
    }

    public void setPorciones(Integer porciones) {
        this.porciones = porciones;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIngredientesEscaladosJson() {
        return ingredientesEscaladosJson;
    }

    public void setIngredientesEscaladosJson(String ingredientesEscaladosJson) {
        this.ingredientesEscaladosJson = ingredientesEscaladosJson;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getTipoCalculado() {
        return tipoCalculado;
    }

    public void setTipoCalculado(Boolean tipoCalculado) {
        this.tipoCalculado = tipoCalculado;
    }
}
