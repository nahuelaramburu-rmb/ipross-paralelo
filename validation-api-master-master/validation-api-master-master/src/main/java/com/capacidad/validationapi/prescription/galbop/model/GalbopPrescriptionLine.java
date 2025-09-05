package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preautorizacionesrenglones")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescriptionLine implements Serializable {

    @EmbeddedId
    private GalbopPrescriptionLineEmbeddedId id;

    @Column(name = "DosisDiaria")
    private Long dailyDosage;

    @Column(name = "Dosis")
    private Long dosage;

    @Column(name = "DosisAutorizada")
    private BigDecimal authorizedDosage;

    @Column(name = "Recomendacion")
    private String recommendation;

    @Column(name = "IDProducto")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPreAutorizacion", insertable = false, updatable = false)
    @JoinColumn(name = "Receta", insertable = false, updatable = false)
    private GalbopPrescriptionDetail prescriptionDetail;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "prescriptionLine")
    private Set<GalbopPrescriptionValidation> prescriptionValidations = new HashSet<>();

}
