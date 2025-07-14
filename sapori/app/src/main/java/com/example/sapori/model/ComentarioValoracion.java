package com.example.sapori.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ComentarioValoracion implements Serializable {

    private Long id;

    // Puntaje de la valoración (1 a 5), puede ser null si solo es comentario
    private Integer puntaje;

    @SerializedName("textoComentario")
    private String textoComentario;

    private Usuario usuario;

    private Receta receta;

    // Para la fecha, según cómo te llegue del backend, puede ser String o LocalDateTime
    private String fecha;

    public ComentarioValoracion() {
    }

    public Long getId() {
        return id;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getTextoComentario() {
        return textoComentario;
    }

    public void setTextoComentario(String textoComentario) {
        this.textoComentario = textoComentario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Receta getReceta() {
        return receta;
    }

    public void setReceta(Receta receta) {
        this.receta = receta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
