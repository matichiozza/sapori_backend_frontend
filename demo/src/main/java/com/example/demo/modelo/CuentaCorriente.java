package com.example.demo.modelo;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas_corrientes")
public class CuentaCorriente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @OneToMany(mappedBy = "cuentaCorriente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pago> facturas = new ArrayList<>();

    @Column(name = "total_facturas")
    private float totalFacturas;

    @Column(name = "saldo_favor")
    private Float saldoFavor = 0f;

    // Constructor
    public CuentaCorriente() {
        this.facturas = new ArrayList<>();
        this.saldoFavor = 0f;
    }

    // Podés calcularlo automáticamente si querés:
    public void recalcularTotal() {
        if (facturas != null) {
            this.totalFacturas = facturas.stream()
                    .filter(pago -> pago.getEstado() == Pago.EstadoPago.A_PAGAR)
                    .map(Pago::getImporte)
                    .reduce(0f, Float::sum);
        } else {
            this.totalFacturas = 0f;
        }
    }

    // Método para calcular total basado en una lista externa de facturas
    public void recalcularTotalConFacturas(List<Pago> facturasExternas) {
        if (facturasExternas != null) {
            this.totalFacturas = facturasExternas.stream()
                    .filter(pago -> pago.getEstado() == Pago.EstadoPago.A_PAGAR)
                    .map(Pago::getImporte)
                    .reduce(0f, Float::sum);
        } else {
            this.totalFacturas = 0f;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public List<Pago> getFacturas() {
        if (facturas == null) {
            facturas = new ArrayList<>();
        }
        return facturas;
    }

    public void setFacturas(List<Pago> facturas) {
        this.facturas = facturas;
    }

    public float getTotalFacturas() {
        return totalFacturas;
    }

    public void setTotalFacturas(float totalFacturas) {
        this.totalFacturas = totalFacturas;
    }

    public Float getSaldoFavor() {
        return saldoFavor;
    }

    public void setSaldoFavor(Float saldoFavor) {
        this.saldoFavor = saldoFavor;
    }
}
