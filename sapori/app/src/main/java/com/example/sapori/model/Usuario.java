package com.example.sapori.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private Long id;
    private String email;
    private String alias;
    private String nombre;
    private String apellido;
    private String password;

    private boolean esAlumno;
    private String fotoPerfil; // URL de Imgur, por ejemplo


    // Constructor vacío requerido por Retrofit/Gson
    public Usuario() {}

    // Constructor parcial
    public Usuario(String email, String alias) {
        this.email = email;
        this.alias = alias;
    }

    public boolean isEsAlumno() {
        return esAlumno;
    }

    public void setEsAlumno(boolean esAlumno) {
        this.esAlumno = esAlumno;
    }

    // Nuevo constructor con todos los datos que usás
    public Usuario(String nombre, String apellido, String email, String alias) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.alias = alias;
    }

    // Getters y setters
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}