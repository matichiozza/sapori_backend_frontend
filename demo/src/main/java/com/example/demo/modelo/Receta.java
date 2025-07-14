package com.example.demo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recetas")
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluye campos null en JSON
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    private String tipo;

    private int porciones;

    private LocalDateTime fechaCreacion;

    private Double calificacion; // puede ser null si no hay calificación

    private Integer tiempo; // tiempo estimado en minutos

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "receta_fotos", joinColumns = @JoinColumn(name = "receta_id"))
    @Column(name = "url_foto")
    private List<String> fotosPlato;

    @Enumerated(EnumType.STRING)
    private EstadoReceta estado; // PENDIENTE, APROBADA, RECHAZADA

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario autor;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<IngredienteReceta> ingredientes;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PasoReceta> pasos;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ComentarioValoracion> comentarios;

    // 🔁 Relación inversa con los usuarios que marcaron esta receta como favorita
    @ManyToMany(mappedBy = "recetasFavoritas", fetch = FetchType.EAGER)
    @JsonIgnore // evita bucles al serializar
    private List<Usuario> usuariosQueLaFavoritaron;




    // Getters y setters

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPorciones() {
        return porciones;
    }

    public void setPorciones(int porciones) {
        this.porciones = porciones;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // Getter que retorna primitivo, devuelve 0.0 si calificacion es null
    public double getCalificacion() {
        return (calificacion != null) ? calificacion : 0.0;
    }

    // Getter que retorna el objeto Double (puede ser null)
    public Double getCalificacionObj() {
        return calificacion;
    }

    // Setter que acepta primitivo
    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    // Setter que acepta objeto Double (puede ser null)
    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Integer getTiempo() {
        return tiempo;
    }

    public void setTiempo(Integer tiempo) {
        this.tiempo = tiempo;
    }

    public List<String> getFotosPlato() {
        return fotosPlato;
    }

    public void setFotosPlato(List<String> fotosPlato) {
        this.fotosPlato = fotosPlato;
    }

    public EstadoReceta getEstado() {
        return estado;
    }

    public void setEstado(EstadoReceta estado) {
        this.estado = estado;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public List<IngredienteReceta> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<IngredienteReceta> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public List<PasoReceta> getPasos() {
        return pasos;
    }

    public void setPasos(List<PasoReceta> pasos) {
        this.pasos = pasos;
    }

    public List<ComentarioValoracion> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ComentarioValoracion> comentarios) {
        this.comentarios = comentarios;
    }

    public List<Usuario> getUsuariosQueLaFavoritaron() {
        return usuariosQueLaFavoritaron;
    }

    public void setUsuariosQueLaFavoritaron(List<Usuario> usuarios) {
        this.usuariosQueLaFavoritaron = usuarios;
    }

    @Transient
    public String getFotoPrincipal() {
        if (fotosPlato != null && !fotosPlato.isEmpty()) {
            return fotosPlato.get(0);
        }
        return null;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Receta receta = (Receta) o;
        return id != null && id.equals(receta.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}