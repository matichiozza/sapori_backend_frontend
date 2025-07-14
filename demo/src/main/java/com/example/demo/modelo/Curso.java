package com.example.demo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "curso_fotos", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "url_foto")
    private List<String> fotosUrl = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    private Float importe;

    private int duracion; // en semanas, meses o lo que definas

    @Column(length = 4000)
    private String objetivo;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    private LocalDateTime fecha_fin;

    @Enumerated(EnumType.STRING)
    private EstadoCurso estadoCurso;

    public enum EstadoCurso {
        EN_CURSO, FINALIZADO, PROXIMAMENTE
    }

    @Column(length = 4000)
    private String descripcion;

    @Column(length = 4000)
    private String temario;

    @Column(length = 4000)
    private String requisitos;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PracticaCurso> practicas = new ArrayList<>();

    // 🔗 Relación con sedes
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "curso_sede",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "sede_id")
    )
    private List<Sede> sedes = new ArrayList<>();

    // 🔗 Alumnos inscriptos
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "curso_inscripcion",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "alumno_id")
    )
    @JsonIgnore
    private List<Alumno> inscriptos = new ArrayList<>();

    // 🔗 Material de clase
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MaterialDeClase> materiales = new ArrayList<>();

    // Getters y setters (igual que antes)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(LocalDateTime fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public EstadoCurso getEstadoCurso() {
        return estadoCurso;
    }

    public void setEstadoCurso(EstadoCurso estadoCurso) {
        this.estadoCurso = estadoCurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getFotosUrl() {
        return fotosUrl;
    }

    public void setFotosUrl(List<String> fotosUrl) {
        this.fotosUrl = fotosUrl;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public Float getImporte() {
        return importe;
    }

    public void setImporte(Float importe) {
        this.importe = importe;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTemario() {
        return temario;
    }

    public void setTemario(String temario) {
        this.temario = temario;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public List<PracticaCurso> getPracticas() {
        return practicas;
    }

    public void setPracticas(List<PracticaCurso> practicas) {
        this.practicas = practicas;
    }

    public List<Sede> getSedes() {
        return sedes;
    }

    public void setSedes(List<Sede> sedes) {
        this.sedes = sedes;
    }

    public List<Alumno> getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(List<Alumno> inscriptos) {
        this.inscriptos = inscriptos;
    }

    public List<MaterialDeClase> getMateriales() {
        return materiales;
    }

    public void setMateriales(List<MaterialDeClase> materiales) {
        this.materiales = materiales;
    }
}
