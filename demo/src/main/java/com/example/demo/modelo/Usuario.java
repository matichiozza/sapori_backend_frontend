package com.example.demo.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(unique = true)
    private String alias;

    private String nombre;

    private String apellido;

    private String password;

    private boolean esAlumno;

    private String fotoPerfil;

    private boolean registroCompleto;

    private String codigoRegistro;

    private LocalDateTime fechaExpiracionCodigo;

    private boolean activo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ComentarioValoracion> comentariosValoraciones;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Receta> recetas;

    @ManyToMany
    @JoinTable(name = "usuario_receta_intento",
            joinColumns        = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "receta_id"))
    @JsonIgnore
    private List<Receta> recetasAIntentar;

    // 🔥 Relación de favoritos
    // En la clase Usuario
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recetas_favoritas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "receta_id")
    )
    @JsonIgnore
    private List<Receta> recetasFavoritas;


    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Alumno alumno;

    public Usuario() {}

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEsAlumno() {
        return esAlumno;
    }

    public void setEsAlumno(boolean esAlumno) {
        this.esAlumno = esAlumno;
    }

    public boolean isRegistroCompleto() {
        return registroCompleto;
    }

    public void setRegistroCompleto(boolean registroCompleto) {
        this.registroCompleto = registroCompleto;
    }

    public String getCodigoRegistro() {
        return codigoRegistro;
    }

    public void setCodigoRegistro(String codigoRegistro) {
        this.codigoRegistro = codigoRegistro;
    }

    public LocalDateTime getFechaExpiracionCodigo() {
        return fechaExpiracionCodigo;
    }

    public void setFechaExpiracionCodigo(LocalDateTime fechaExpiracionCodigo) {
        this.fechaExpiracionCodigo = fechaExpiracionCodigo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ComentarioValoracion> getComentariosValoraciones() {
        return comentariosValoraciones;
    }

    public void setComentariosValoraciones(List<ComentarioValoracion> comentariosValoraciones) {
        this.comentariosValoraciones = comentariosValoraciones;
    }

    public List<Receta> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<Receta> recetas) {
        this.recetas = recetas;
    }

    public List<Receta> getRecetasAIntentar() {
        return recetasAIntentar;
    }

    public void setRecetasAIntentar(List<Receta> recetasAIntentar) {
        this.recetasAIntentar = recetasAIntentar;
    }

    public List<Receta> getRecetasFavoritas() {
        return recetasFavoritas;
    }

    public void setRecetasFavoritas(List<Receta> recetasFavoritas) {
        this.recetasFavoritas = recetasFavoritas;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (alumno != null) {
            this.esAlumno = true;
        }
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
    

    @Override
    public String toString() {
        return "Usuario{email=" + email + ", alias=" + alias + "}";
    }
}