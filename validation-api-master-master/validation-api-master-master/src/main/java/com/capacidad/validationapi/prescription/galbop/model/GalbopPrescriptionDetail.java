package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preautorizacionesdetalle")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescriptionDetail implements Serializable {

    @EmbeddedId
    private GalbopPrescriptionDetailEmbeddedId id;

    @Column(name = "NroReceta")
    private String prescriptionNumber;

    @Column(name = "CodAutorizacion")
    private Long authorizationId;

    @Column(name = "DisponibleDesde")
    private LocalDate dateFrom;

    @Column(name = "DisponibleHasta")
    private LocalDate dateTo;

    @Column(name = "Estado")
    private String status;

    @Column(name = "FechaEstado")
    private LocalDateTime statusDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPreAutorizacion", referencedColumnName = "IDPreAutorizacion", insertable = false, updatable = false)
    private GalbopPrescription prescription;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "prescriptionDetail")
    private Set<GalbopPrescriptionLine> prescriptionLines = new HashSet<>();

}
