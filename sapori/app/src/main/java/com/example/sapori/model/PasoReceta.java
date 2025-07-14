package com.example.sapori.model;

import java.io.Serializable;
import java.util.List;

public class PasoReceta implements Serializable {

    private Long id;
    private int numeroPaso;
    private String descripcion;
    private List<String> mediaUrls;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNumeroPaso() { return numeroPaso; }
    public void setNumeroPaso(int numeroPaso) { this.numeroPaso = numeroPaso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
}
